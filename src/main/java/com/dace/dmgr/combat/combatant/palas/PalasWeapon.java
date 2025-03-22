package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.AimModule;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

public final class PalasWeapon extends AbstractWeapon implements Reloadable, Aimable {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-PalasWeaponInfo.AIM_SLOW);

    /** 재장전 모듈 */
    @NonNull
    @Getter
    private final ReloadModule reloadModule;
    /** 정조준 모듈 */
    @NonNull
    @Getter
    private final AimModule aimModule;
    /** 사용 후 쿨타임 진행 여부 */
    private boolean isActionCooldown = true;

    public PalasWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, PalasWeaponInfo.getInstance(), PalasWeaponInfo.COOLDOWN);

        this.reloadModule = new ReloadModule(this, PalasWeaponInfo.CAPACITY, PalasWeaponInfo.RELOAD_DURATION);
        this.aimModule = new AimModule(this, PalasWeaponInfo.ZOOM_LEVEL);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK, ActionKey.DROP};
    }

    @Override
    @NonNull
    public String getActionBarString() {
        return reloadModule.getActionBarProgressBar(reloadModule.getCapacity(), '┃') + (isActionCooldown ? " §a■" : " §c□");
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

                new PalasWeaponHitscan(aimModule.isAiming()).shot();
                new PalasWeaponHealHitscan(aimModule.isAiming()).shot();

                reloadModule.cancel();
                isActionCooldown = false;

                CombatUtil.sendRecoil(combatUser, PalasWeaponInfo.RECOIL.UP, PalasWeaponInfo.RECOIL.SIDE, PalasWeaponInfo.RECOIL.UP_SPREAD,
                        PalasWeaponInfo.RECOIL.SIDE_SPREAD, 2, 1);
                PalasWeaponInfo.SOUND.USE.play(combatUser.getLocation());

                addActionTask(new DelayTask(this::action, getDefaultCooldown().toTicks()));

                break;
            }
            case RIGHT_CLICK: {
                if (aimModule.isAiming()) {
                    cancel();
                    return;
                }

                cancel();
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
    protected void onCancelled() {
        reloadModule.cancel();
        aimModule.cancel();
    }

    @Override
    public boolean canReload() {
        return reloadModule.getRemainingAmmo() < reloadModule.getCapacity();
    }

    @Override
    public void onAmmoEmpty() {
        if (reloadModule.isReloading())
            return;

        cancel();
        reloadModule.reload();
    }

    @Override
    public void onReloadTick(long i) {
        PalasWeaponInfo.SOUND.RELOAD.play(i, combatUser.getLocation());
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    @Override
    public void onAimEnable() {
        combatUser.setGlobalCooldown(PalasWeaponInfo.AIM_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        PalasWeaponInfo.SOUND.AIM_ON.play(combatUser.getLocation());
    }

    @Override
    public void onAimDisable() {
        combatUser.setGlobalCooldown(PalasWeaponInfo.AIM_DURATION);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);

        PalasWeaponInfo.SOUND.AIM_OFF.play(combatUser.getLocation());
    }

    /**
     * 사용 후 쿨타임 작업을 수행한다.
     */
    private void action() {
        setCooldown(PalasWeaponInfo.ACTION_COOLDOWN);

        reloadModule.cancel();

        addActionTask(new IntervalTask(i -> {
            PalasWeaponInfo.SOUND.ACTION.play(i, combatUser.getLocation());

            switch ((int) i) {
                case 1:
                    combatUser.addYawAndPitch(-0.25, 0.1);
                    break;
                case 2:
                    combatUser.addYawAndPitch(-0.1, 0.2);
                    break;
                case 5:
                    combatUser.addYawAndPitch(0.1, -0.2);
                    break;
                case 6:
                    combatUser.addYawAndPitch(0.25, -0.1);
                    break;
                default:
                    break;
            }
        }, () -> {
            isActionCooldown = true;
            reloadModule.consume(1);
        }, 1, PalasWeaponInfo.ACTION_COOLDOWN.toTicks()));
    }

    private final class PalasWeaponHitscan extends Hitscan<Damageable> {
        private PalasWeaponHitscan(boolean isAiming) {
            super(combatUser, CombatUtil.EntityCondition.enemy(combatUser).or(CombatUtil.EntityCondition.team(combatUser).exclude(combatUser)),
                    (isAiming ? Option.builder() : Option.builder().maxDistance(PalasWeaponInfo.DISTANCE)).build());
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(8, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, (aimModule.isAiming() ? 0 : 0.2), -0.2, 0);
                PalasWeaponInfo.PARTICLE.BULLET_TRAIL.play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.playBulletHitBlockEffect(location, hitBlock);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.isEnemy(combatUser)) {
                    target.getDamageModule().damage(combatUser, PalasWeaponInfo.DAMAGE, DamageType.NORMAL, location, false, true);

                    PalasWeaponInfo.PARTICLE.HIT_ENTITY.play(location);
                }

                return false;
            };
        }
    }

    private final class PalasWeaponHealHitscan extends Hitscan<Damageable> {
        private PalasWeaponHealHitscan(boolean isAiming) {
            super(combatUser, CombatUtil.EntityCondition.enemy(combatUser).or(CombatUtil.EntityCondition.team(combatUser).exclude(combatUser)),
                    (isAiming ? Option.builder() : Option.builder().maxDistance(PalasWeaponInfo.DISTANCE)).size(PalasWeaponInfo.HEAL_SIZE).build());
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return (location, i) -> true;
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target instanceof Healable && !target.isEnemy(combatUser)) {
                    PalasP1 skillp1 = combatUser.getSkill(PalasP1Info.getInstance());
                    skillp1.setHealAmount(PalasWeaponInfo.HEAL);
                    skillp1.setTarget((Healable) target);

                    combatUser.useAction(ActionKey.PERIODIC_1);

                    ((Healable) target).getDamageModule().heal(combatUser, PalasWeaponInfo.HEAL, true);

                    PalasWeaponInfo.PARTICLE.HIT_ENTITY.play(location);
                }

                return false;
            };
        }
    }
}
