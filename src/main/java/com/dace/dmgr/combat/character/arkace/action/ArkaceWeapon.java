package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.GradualSpreadModule;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.GunHitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@Getter
public final class ArkaceWeapon extends AbstractWeapon implements Reloadable, FullAuto {
    /** 재장전 모듈 */
    @NonNull
    private final ReloadModule reloadModule;
    /** 연사 모듈 */
    @NonNull
    private final GradualSpreadModule fullAutoModule;

    public ArkaceWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceWeaponInfo.getInstance());

        reloadModule = new ReloadModule(this, ArkaceWeaponInfo.CAPACITY, ArkaceWeaponInfo.RELOAD_DURATION);
        fullAutoModule = new GradualSpreadModule(this, ActionKey.RIGHT_CLICK, ArkaceWeaponInfo.FIRE_RATE, ArkaceWeaponInfo.SPREAD.INCREMENT,
                ArkaceWeaponInfo.SPREAD.START, ArkaceWeaponInfo.SPREAD.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.RIGHT_CLICK, ActionKey.DROP};
    }

    @Override
    public long getDefaultCooldown() {
        return 0;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case RIGHT_CLICK: {
                if (reloadModule.getRemainingAmmo() == 0) {
                    onAmmoEmpty();
                    return;
                }
                if (cancelP1()) {
                    setCooldown(ArkaceWeaponInfo.SPRINT_READY_DURATION);
                    return;
                }

                Location loc = combatUser.getEntity().getLocation();
                if (combatUser.getSkill(ArkaceUltInfo.getInstance()).isDurationFinished()) {
                    double spread = combatUser.isMoving() ? fullAutoModule.increaseSpread() : 0;
                    if (combatUser.getEntity().isSprinting() || !combatUser.getEntity().isOnGround())
                        spread *= ArkaceWeaponInfo.SPREAD.SPRINT_MULTIPLIER;

                    Vector dir = VectorUtil.getSpreadedVector(loc.getDirection(), spread);
                    new ArkaceWeaponHitscan(false).shoot(dir);
                    reloadModule.consume(1);

                    CombatUtil.setRecoil(combatUser, ArkaceWeaponInfo.RECOIL.UP, ArkaceWeaponInfo.RECOIL.SIDE, ArkaceWeaponInfo.RECOIL.UP_SPREAD,
                            ArkaceWeaponInfo.RECOIL.SIDE_SPREAD, 2, 2);
                    ArkaceWeaponInfo.SOUND.USE.play(loc);
                    TaskUtil.addTask(this, new DelayTask(() -> CombatEffectUtil.SHELL_DROP_SOUND.play(loc), 8));
                } else {
                    new ArkaceWeaponHitscan(true).shoot();

                    ArkaceWeaponInfo.SOUND.USE_ULT.play(loc);
                }

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
    }

    /**
     * 패시브 1번 스킬을 취소시킨다.
     *
     * @return 무기 사용 취소 여부
     */
    private boolean cancelP1() {
        ArkaceP1 skillp1 = combatUser.getSkill(ArkaceP1Info.getInstance());
        long skillp1Cooldown = ArkaceWeaponInfo.SPRINT_READY_DURATION + 2;

        if (skillp1.isCancellable()) {
            skillp1.onCancelled();
            skillp1.setCooldown(skillp1Cooldown);

            return true;
        }

        skillp1.setCooldown(skillp1Cooldown);
        return false;
    }

    @Override
    public boolean canReload() {
        return reloadModule.getRemainingAmmo() < ArkaceWeaponInfo.CAPACITY;
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
        ArkaceWeaponInfo.SOUND.RELOAD.play(i, combatUser.getEntity().getLocation());
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    @Override
    public void reset() {
        super.reset();

        reloadModule.setRemainingAmmo(ArkaceWeaponInfo.CAPACITY);
    }

    private final class ArkaceWeaponHitscan extends GunHitscan {
        private final boolean isUlt;
        private double distance = 0;

        private ArkaceWeaponHitscan(boolean isUlt) {
            super(combatUser, HitscanOption.builder().condition(combatUser::isEnemy).build());
            this.isUlt = isUlt;
        }

        @Override
        protected boolean onInterval() {
            distance += getVelocity().length();
            return super.onInterval();
        }

        @Override
        protected void onTrailInterval() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0.2, -0.2, 0);
            (isUlt ? ArkaceWeaponInfo.PARTICLE.BULLET_TRAIL_ULT : CombatEffectUtil.BULLET_TRAIL_PARTICLE).play(loc);
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (isUlt)
                target.getDamageModule().damage(combatUser, ArkaceWeaponInfo.DAMAGE, DamageType.NORMAL, getLocation(), isCrit, false);
            else {
                double damage = CombatUtil.getDistantDamage(ArkaceWeaponInfo.DAMAGE, distance, ArkaceWeaponInfo.DAMAGE_WEAKENING_DISTANCE);
                target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, getLocation(), isCrit, true);
            }

            return false;
        }
    }
}
