package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.AimModule;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.character.palas.Palas;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.GunHitscan;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;

@Getter
public final class PalasWeapon extends AbstractWeapon implements Reloadable, Aimable {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "PalasWeaponL";

    /** 재장전 모듈 */
    @NonNull
    private final ReloadModule reloadModule;
    /** 정조준 모듈 */
    @NonNull
    private final AimModule aimModule;
    /** 사용 후 쿨타임 진행 여부 */
    private boolean isActionCooldown = true;

    public PalasWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, PalasWeaponInfo.getInstance());

        reloadModule = new ReloadModule(this, PalasWeaponInfo.CAPACITY, PalasWeaponInfo.RELOAD_DURATION);
        aimModule = new AimModule(this, PalasWeaponInfo.ZOOM_LEVEL);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK, ActionKey.DROP};
    }

    @Override
    public long getDefaultCooldown() {
        return PalasWeaponInfo.COOLDOWN;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return (actionKey == ActionKey.DROP || actionKey == ActionKey.RIGHT_CLICK ? combatUser.isGlobalCooldownFinished() : super.canUse(actionKey));
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case LEFT_CLICK: {
                if (reloadModule.getRemainingAmmo() == 0) {
                    onAmmoEmpty();
                    return;
                }
                if (!isActionCooldown) {
                    action();
                    break;
                }

                setCooldown();

                new PalasWeaponHitscan(aimModule.isAiming()).shoot();
                new PalasWeaponHealHitscan(aimModule.isAiming()).shoot();
                reloadModule.cancel();
                isActionCooldown = false;

                CombatUtil.setRecoil(combatUser, PalasWeaponInfo.RECOIL.UP, PalasWeaponInfo.RECOIL.SIDE, PalasWeaponInfo.RECOIL.UP_SPREAD,
                        PalasWeaponInfo.RECOIL.SIDE_SPREAD, 2, 1);
                PalasWeaponInfo.SOUND.USE.play(combatUser.getEntity().getLocation());

                TaskUtil.addTask(taskRunner, new DelayTask(this::action, getDefaultCooldown()));

                break;
            }
            case RIGHT_CLICK: {
                setCooldown(2);
                if (aimModule.isAiming()) {
                    onCancelled();
                    return;
                }

                onCancelled();
                aimModule.toggleAim();

                break;
            }
            case DROP: {
                onAmmoEmpty();

                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        reloadModule.cancel();
        aimModule.cancel();
    }

    /**
     * 사용 후 쿨타임 작업을 수행한다.
     */
    private void action() {
        setCooldown(PalasWeaponInfo.ACTION_COOLDOWN);

        reloadModule.cancel();

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            PalasWeaponInfo.SOUND.ACTION.play(i, combatUser.getEntity().getLocation());

            switch ((int) i) {
                case 1:
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), -0.25, 0.1);
                    break;
                case 2:
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), -0.1, 0.2);
                    break;
                case 5:
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), 0.1, -0.2);
                    break;
                case 6:
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), 0.25, -0.1);
                    break;
                default:
                    break;
            }
        }, () -> {
            isActionCooldown = true;
            reloadModule.consume(1);
        }, 1, PalasWeaponInfo.ACTION_COOLDOWN));
    }

    @Override
    public boolean canReload() {
        return reloadModule.getRemainingAmmo() < PalasWeaponInfo.CAPACITY;
    }

    @Override
    public void onAmmoEmpty() {
        if (reloadModule.isReloading())
            return;

        onCancelled();
        reloadModule.reload();
    }

    @Override
    public void onReloadTick(long i) {
        PalasWeaponInfo.SOUND.RELOAD.play(i, combatUser.getEntity().getLocation());
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    @Override
    public void onAimEnable() {
        combatUser.setGlobalCooldown((int) PalasWeaponInfo.AIM_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -PalasWeaponInfo.AIM_SLOW);

        PalasWeaponInfo.SOUND.AIM_ON.play(combatUser.getEntity().getLocation());
    }

    @Override
    public void onAimDisable() {
        combatUser.setGlobalCooldown((int) PalasWeaponInfo.AIM_DURATION);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);

        PalasWeaponInfo.SOUND.AIM_OFF.play(combatUser.getEntity().getLocation());
    }

    @Override
    public void reset() {
        super.reset();

        reloadModule.setRemainingAmmo(PalasWeaponInfo.CAPACITY);
    }

    private final class PalasWeaponHitscan extends GunHitscan {
        private PalasWeaponHitscan(boolean isAiming) {
            super(combatUser, (isAiming ? HitscanOption.builder() : HitscanOption.builder().maxDistance(PalasWeaponInfo.DISTANCE)).trailInterval(8)
                    .condition(combatEntity -> Palas.getTargetedActionCondition(PalasWeapon.this.combatUser, combatEntity)
                            || combatEntity.isEnemy(PalasWeapon.this.combatUser)).build());
        }

        @Override
        protected void onTrailInterval() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), (aimModule.isAiming() ? 0 : 0.2), -0.2, 0);
            PalasWeaponInfo.PARTICLE.BULLET_TRAIL.play(loc);
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.isEnemy(combatUser)) {
                target.getDamageModule().damage(combatUser, PalasWeaponInfo.DAMAGE, DamageType.NORMAL, getLocation(), false, true);

                PalasWeaponInfo.PARTICLE.HIT_ENTITY.play(getLocation());
            }

            return false;
        }
    }

    private final class PalasWeaponHealHitscan extends Hitscan {
        private PalasWeaponHealHitscan(boolean isAiming) {
            super(combatUser, (isAiming ? HitscanOption.builder() : HitscanOption.builder().maxDistance(PalasWeaponInfo.DISTANCE))
                    .size(PalasWeaponInfo.HEAL_SIZE)
                    .condition(combatEntity -> Palas.getTargetedActionCondition(PalasWeapon.this.combatUser, combatEntity)
                            || combatEntity.isEnemy(PalasWeapon.this.combatUser)).build());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target instanceof Healable && !target.isEnemy(combatUser)) {
                if (((Healable) target).getDamageModule().heal(combatUser, PalasWeaponInfo.HEAL, true) && target.getDamageModule().isLowHealth()) {
                    PalasP1 skillp1 = combatUser.getSkill(PalasP1Info.getInstance());
                    skillp1.setHealAmount(PalasWeaponInfo.HEAL);
                    skillp1.setTarget((Healable) target);
                    combatUser.useAction(ActionKey.PERIODIC_1);
                }

                PalasWeaponInfo.PARTICLE.HIT_ENTITY.play(getLocation());
            }

            return false;
        }
    }
}
