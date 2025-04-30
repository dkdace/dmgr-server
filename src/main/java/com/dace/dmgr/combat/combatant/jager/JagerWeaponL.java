package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.module.AimModule;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.action.weapon.module.SwapModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

@Getter
public final class JagerWeaponL extends AbstractWeapon implements Reloadable, Swappable<JagerWeaponR>, Aimable {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-JagerWeaponInfo.AIM_SLOW);

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
        super(combatUser, JagerWeaponInfo.getInstance(), JagerWeaponInfo.COOLDOWN);

        this.reloadModule = new ReloadModule(this, JagerWeaponInfo.CAPACITY, JagerWeaponInfo.RELOAD_DURATION);
        this.swapModule = new SwapModule<>(this, new JagerWeaponR(combatUser, this), JagerWeaponInfo.SWAP_DURATION);
        this.aimModule = new AimModule(this, JagerWeaponInfo.Scope.ZOOM_LEVEL);

        addOnReset(() -> swapModule.getSubweapon().getReloadModule().resetRemainingAmmo());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK, ActionKey.DROP};
    }

    @Override
    @NonNull
    public String getActionBarString() {
        String text = reloadModule.getActionBarProgressBar(reloadModule.getCapacity(), '*');
        if (!swapModule.isSwapped())
            text = "§a" + text;

        return text;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ActionManager actionManager = combatUser.getActionManager();
        return (actionKey == ActionKey.DROP || actionKey == ActionKey.RIGHT_CLICK ? combatUser.isGlobalCooldownFinished() : super.canUse(actionKey))
                && !actionManager.getSkill(JagerA1Info.getInstance()).getConfirmModule().isChecking()
                && actionManager.getSkill(JagerA3Info.getInstance()).isDurationFinished();
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

                new JagerWeaponLProjectile().shot();

                reloadModule.consume(1);

                CombatUtil.sendRecoil(combatUser, JagerWeaponInfo.Recoil.UP, JagerWeaponInfo.Recoil.SIDE, JagerWeaponInfo.Recoil.UP_SPREAD,
                        JagerWeaponInfo.Recoil.SIDE_SPREAD, 2, 1);
                JagerWeaponInfo.Sounds.USE.play(combatUser.getLocation());

                break;
            }
            case RIGHT_CLICK: {
                cancel();

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
    protected void onCancelled() {
        if (swapModule.isSwapped()) {
            swapModule.getSubweapon().cancel();
            return;
        }

        reloadModule.cancel();
        swapModule.cancel();
        aimModule.cancel();
    }

    @Override
    public boolean canReload() {
        return reloadModule.getRemainingAmmo() < reloadModule.getCapacity()
                || swapModule.getSubweapon().getReloadModule().getRemainingAmmo() < swapModule.getSubweapon().getReloadModule().getCapacity();
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
        JagerWeaponInfo.Sounds.RELOAD.play(i, combatUser.getLocation());
    }

    @Override
    public void onReloadFinished() {
        swapModule.getSubweapon().getReloadModule().resetRemainingAmmo();
    }

    @Override
    public void onSwapStart(boolean isSwapped) {
        setCooldown(JagerWeaponInfo.SWAP_DURATION);
    }

    @Override
    public void onSwapFinished(boolean isSwapped) {
        // 미사용
    }

    @Override
    public void onAimEnable() {
        combatUser.setGlobalCooldown(JagerWeaponInfo.SWAP_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        JagerWeaponInfo.Sounds.AIM_ON.play(combatUser.getLocation());
    }

    @Override
    public void onAimDisable() {
        combatUser.setGlobalCooldown(JagerWeaponInfo.SWAP_DURATION);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);

        JagerWeaponInfo.Sounds.AIM_OFF.play(combatUser.getLocation());
    }

    private final class JagerWeaponLProjectile extends Projectile<Damageable> {
        private JagerWeaponLProjectile() {
            super(JagerWeaponL.this, JagerWeaponInfo.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().maxDistance(JagerWeaponInfo.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            JagerWeaponInfo.Particles.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(10, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                JagerWeaponInfo.Particles.BULLET_TRAIL.play(loc);
            });
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
                if (target.getDamageModule().damage(this, JagerWeaponInfo.DAMAGE, DamageType.NORMAL, location, false, true))
                    JagerT1.addFreezeValue(target, JagerWeaponInfo.FREEZE);

                return false;
            };
        }
    }
}
