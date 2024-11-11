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
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

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
        return (actionKey == ActionKey.DROP ? combatUser.isGlobalCooldownFinished() : super.canUse(actionKey));
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

                double spread = combatUser.isMoving() && !aimModule.isAiming() ? PalasWeaponInfo.SPREAD : 0;
                if (combatUser.getEntity().isSprinting() || !combatUser.getEntity().isOnGround())
                    spread *= PalasWeaponInfo.SPREAD_SPRINT_MULTIPLIER;

                Vector dir = VectorUtil.getSpreadedVector(combatUser.getEntity().getLocation().getDirection(), spread);
                new PalasWeaponHitscan().shoot(dir);
                new PalasWeaponHealHitscan().shoot(dir);
                reloadModule.cancel();
                isActionCooldown = false;

                CombatUtil.setRecoil(combatUser, PalasWeaponInfo.RECOIL.UP, PalasWeaponInfo.RECOIL.SIDE, PalasWeaponInfo.RECOIL.UP_SPREAD,
                        PalasWeaponInfo.RECOIL.SIDE_SPREAD, 2, 1);
                SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_WEAPON_USE, combatUser.getEntity().getLocation());

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
            switch (i.intValue()) {
                case 1:
                    SoundUtil.play(Sound.ENTITY_VILLAGER_YES, combatUser.getEntity().getLocation(), 0.6, 1.2);
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), -0.4, 0.1);
                    break;
                case 2:
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), -0.6, 0.15);
                    break;
                case 3:
                    SoundUtil.play(Sound.BLOCK_LAVA_EXTINGUISH, combatUser.getEntity().getLocation(), 0.5, 1.4);
                    break;
                case 5:
                    SoundUtil.play(Sound.ENTITY_VILLAGER_NO, combatUser.getEntity().getLocation(), 0.6, 1.2);
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), 0.4, -0.1);
                    break;
                case 6:
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), 0.6, -0.15);
                    break;
                default:
                    break;
            }

            return true;
        }, isCancelled -> {
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
        switch ((int) i) {
            case 3:
                SoundUtil.play("new.ui.stonecutter.take_result", combatUser.getEntity().getLocation(), 0.6, 1.5);
                break;
            case 6:
                SoundUtil.play(Sound.BLOCK_PISTON_CONTRACT, combatUser.getEntity().getLocation(), 0.6, 1.3);
                break;
            case 10:
                SoundUtil.play(Sound.ITEM_FLINTANDSTEEL_USE, combatUser.getEntity().getLocation(), 0.6, 0.8);
                break;
            case 12:
                SoundUtil.play(Sound.ITEM_BOTTLE_EMPTY, combatUser.getEntity().getLocation(), 0.6, 1.4);
                break;
            case 14:
                SoundUtil.play(Sound.BLOCK_IRON_TRAPDOOR_OPEN, combatUser.getEntity().getLocation(), 0.6, 1.3);
                break;
            case 22:
                SoundUtil.play(Sound.ENTITY_PLAYER_HURT, combatUser.getEntity().getLocation(), 0.6, 0.5);
                break;
            case 24:
                SoundUtil.play(Sound.ENTITY_CAT_PURREOW, combatUser.getEntity().getLocation(), 0.6, 1.8);
                break;
            case 32:
                SoundUtil.play(Sound.ITEM_BOTTLE_FILL, combatUser.getEntity().getLocation(), 0.6, 1.4);
                break;
            case 38:
                SoundUtil.play("new.block.chain.place", combatUser.getEntity().getLocation(), 0.6, 1.6);
                break;
            case 41:
                SoundUtil.play(Sound.BLOCK_IRON_TRAPDOOR_CLOSE, combatUser.getEntity().getLocation(), 0.6, 1.2);
                break;
            default:
                break;
        }
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    @Override
    public void onAimEnable() {
        combatUser.setGlobalCooldown((int) PalasWeaponInfo.AIM_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -PalasWeaponInfo.AIM_SLOW);

        SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_WEAPON_AIM_ON, combatUser.getEntity().getLocation());
    }

    @Override
    public void onAimDisable() {
        combatUser.setGlobalCooldown((int) PalasWeaponInfo.AIM_DURATION);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);

        SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_WEAPON_AIM_OFF, combatUser.getEntity().getLocation());
    }

    @Override
    public void reset() {
        super.reset();

        reloadModule.setRemainingAmmo(PalasWeaponInfo.CAPACITY);
    }

    private final class PalasWeaponHitscan extends GunHitscan {
        private PalasWeaponHitscan() {
            super(combatUser, HitscanOption.builder().trailInterval(8).condition(combatEntity ->
                    Palas.getTargetedActionCondition(PalasWeapon.this.combatUser, combatEntity) || combatEntity.isEnemy(PalasWeapon.this.combatUser)).build());
        }

        @Override
        protected void onTrailInterval() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), (aimModule.isAiming() ? 0 : 0.2), -0.2, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 1,
                    0, 0, 0, 210, 160, 70);
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.isEnemy(combatUser)) {
                target.getDamageModule().damage(combatUser, PalasWeaponInfo.DAMAGE, DamageType.NORMAL, getLocation(), false, true);

                ParticleUtil.play(Particle.WATER_SPLASH, getLocation(), 15, 0, 0, 0, 0);
            }

            return false;
        }
    }

    private final class PalasWeaponHealHitscan extends Hitscan {
        private PalasWeaponHealHitscan() {
            super(combatUser, HitscanOption.builder().size(PalasWeaponInfo.HEAL_SIZE).condition(combatEntity ->
                    Palas.getTargetedActionCondition(PalasWeapon.this.combatUser, combatEntity) || combatEntity.isEnemy(PalasWeapon.this.combatUser)).build());
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

                ParticleUtil.play(Particle.WATER_SPLASH, getLocation(), 15, 0, 0, 0, 0);
            }

            return false;
        }
    }
}
