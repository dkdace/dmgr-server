package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.module.AimModule;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.action.weapon.module.SwapModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

@Getter
public final class JagerWeaponL extends AbstractWeapon implements Reloadable, Swappable<JagerWeaponR>, Aimable {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "JagerWeaponL";
    /** 재장전 모듈 */
    @NonNull
    private final ReloadModule reloadModule;
    /** 2중 무기 모듈 */
    @NonNull
    private final SwapModule<JagerWeaponR> swapModule;
    /** 정조준 모듈 */
    @NonNull
    private final AimModule aimModule;

    public JagerWeaponL(@NonNull CombatUser combatUser) {
        super(combatUser, JagerWeaponInfo.getInstance());

        reloadModule = new ReloadModule(this, JagerWeaponInfo.CAPACITY, JagerWeaponInfo.RELOAD_DURATION);
        swapModule = new SwapModule<>(this, new JagerWeaponR(combatUser, this), JagerWeaponInfo.SWAP_DURATION);
        aimModule = new AimModule(this, JagerWeaponInfo.SCOPE.ZOOM_LEVEL);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK, ActionKey.DROP};
    }

    @Override
    public long getDefaultCooldown() {
        return JagerWeaponInfo.COOLDOWN;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return (actionKey == ActionKey.DROP ? combatUser.isGlobalCooldownFinished() : super.canUse(actionKey))
                && !combatUser.getSkill(JagerA1Info.getInstance()).getConfirmModule().isChecking()
                && combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case LEFT_CLICK: {
                if (reloadModule.getRemainingAmmo() == 0) {
                    onAmmoEmpty();
                    return;
                }

                setCooldown();

                double spread = combatUser.isMoving() ? JagerWeaponInfo.SPREAD : 0;
                if (combatUser.getEntity().isSprinting() || !combatUser.getEntity().isOnGround())
                    spread *= JagerWeaponInfo.SPREAD_SPRINT_MULTIPLIER;

                Vector dir = VectorUtil.getSpreadedVector(combatUser.getEntity().getLocation().getDirection(), spread);
                new JagerWeaponLProjectile().shoot(dir);
                reloadModule.consume(1);

                CombatUtil.setRecoil(combatUser, JagerWeaponInfo.RECOIL.UP, JagerWeaponInfo.RECOIL.SIDE, JagerWeaponInfo.RECOIL.UP_SPREAD,
                        JagerWeaponInfo.RECOIL.SIDE_SPREAD, 2, 1);
                JagerWeaponInfo.SOUND.USE.play(combatUser.getEntity().getLocation());

                break;
            }
            case RIGHT_CLICK: {
                setCooldown(2);
                onCancelled();

                aimModule.toggleAim();
                swapModule.swap();

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
        if (swapModule.getSwapState() == SwapState.SECONDARY) {
            swapModule.getSubweapon().onCancelled();
            return;
        }

        super.onCancelled();

        reloadModule.cancel();
        swapModule.cancel();
        aimModule.cancel();
    }

    @Override
    public boolean canReload() {
        return reloadModule.getRemainingAmmo() < JagerWeaponInfo.CAPACITY
                || swapModule.getSubweapon().getReloadModule().getRemainingAmmo() < JagerWeaponInfo.SCOPE.CAPACITY;
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
        JagerWeaponInfo.SOUND.RELOAD.play(i, combatUser.getEntity().getLocation());
    }

    @Override
    public void onReloadFinished() {
        swapModule.getSubweapon().getReloadModule().setRemainingAmmo(JagerWeaponInfo.SCOPE.CAPACITY);
    }

    @Override
    public void onSwapStart(@NonNull SwapState swapState) {
        setCooldown(JagerWeaponInfo.SWAP_DURATION);

        (swapState == SwapState.PRIMARY ? JagerWeaponInfo.SOUND.SWAP_OFF : JagerWeaponInfo.SOUND.SWAP_ON).play(combatUser.getEntity().getLocation());
    }

    @Override
    public void onSwapFinished(@NonNull SwapState swapState) {
        // 미사용
    }

    @Override
    public void onAimEnable() {
        combatUser.setGlobalCooldown((int) JagerWeaponInfo.SWAP_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -JagerWeaponInfo.AIM_SLOW);
    }

    @Override
    public void onAimDisable() {
        combatUser.setGlobalCooldown((int) JagerWeaponInfo.SWAP_DURATION);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
    }

    @Override
    public void reset() {
        super.reset();

        reloadModule.setRemainingAmmo(JagerWeaponInfo.CAPACITY);
        swapModule.getSubweapon().getReloadModule().setRemainingAmmo(JagerWeaponInfo.SCOPE.CAPACITY);
    }

    private final class JagerWeaponLProjectile extends Projectile {
        private JagerWeaponLProjectile() {
            super(combatUser, JagerWeaponInfo.VELOCITY, ProjectileOption.builder().trailInterval(10)
                    .maxDistance(JagerWeaponInfo.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0.2, -0.2, 0);
            JagerWeaponInfo.PARTICLE.BULLET_TRAIL.play(loc);
        }

        @Override
        protected void onHit() {
            JagerWeaponInfo.PARTICLE.HIT.play(getLocation());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.getDamageModule().damage(this, JagerWeaponInfo.DAMAGE, DamageType.NORMAL, getLocation(), false, true))
                JagerT1.addFreezeValue(target, JagerWeaponInfo.FREEZE);

            return false;
        }
    }
}
