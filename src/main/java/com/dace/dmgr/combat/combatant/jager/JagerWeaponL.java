package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.handler.DropHandler;
import com.dace.dmgr.combat.ability.handler.LeftClickHandler;
import com.dace.dmgr.combat.ability.handler.RightClickHandler;
import com.dace.dmgr.combat.ability.weapon.AbstractWeapon;
import com.dace.dmgr.combat.ability.weapon.Aimable;
import com.dace.dmgr.combat.ability.weapon.Reloadable;
import com.dace.dmgr.combat.ability.weapon.Swappable;
import com.dace.dmgr.combat.ability.weapon.module.AimModule;
import com.dace.dmgr.combat.ability.weapon.module.ReloadModule;
import com.dace.dmgr.combat.ability.weapon.module.SwapModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

@Getter
public final class JagerWeaponL extends AbstractWeapon implements Reloadable, Swappable<JagerWeaponR>, Aimable, LeftClickHandler, RightClickHandler, DropHandler {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-JagerWeaponInfo.AIM_SLOW);

    /** 재장전 모듈 */
    @NonNull
    private final ReloadModule reloadModule;
    /** 2중 무기 모듈 */
    @NonNull
    private final SwapModule<JagerWeaponR> swapModule;
    /** 보조무기 */
    @NonNull
    private final JagerWeaponR subweapon;
    /** 정조준 모듈 */
    @NonNull
    private final AimModule aimModule;

    public JagerWeaponL(@NonNull CombatUser combatUser, @NonNull JagerWeaponInfo weaponInfo) {
        super(combatUser, weaponInfo, JagerWeaponInfo.COOLDOWN);

        this.reloadModule = new ReloadModule(this);
        this.swapModule = new SwapModule<>(this);
        this.subweapon = new JagerWeaponR(combatUser, weaponInfo, this);
        this.aimModule = new AimModule(this);

        addOnReset(() -> subweapon.getReloadModule().resetRemainingAmmo());
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        return ActionBarDisplay.builder(this).ammoBar(getCapacity(), '*').build();
    }

    @Override
    protected boolean canUse() {
        AbilityManager abilityManager = combatUser.getAbilityManager();

        return super.canUse() && !abilityManager.getAbility(JagerA1Info.getInstance()).getConfirmModule().isChecking()
                && abilityManager.getAbility(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onLeftClick() {
        if (!reloadModule.consume(1))
            return;

        setCooldown();

        new JagerWeaponLProjectile().shot();

        JagerWeaponInfo.RECOIL.send(combatUser);
        JagerWeaponInfo.Effects.USE.play(combatUser.getLocation());
    }

    @Override
    public boolean isRightClickIgnoreCooldown() {
        return true;
    }

    @Override
    public void onRightClick() {
        cancel();

        aimModule.toggleAim();
        swapModule.swap();
    }

    @Override
    public boolean isDropIgnoreCooldown() {
        return true;
    }

    @Override
    public void onDrop() {
        reloadModule.reload();
    }

    @Override
    protected void onCancelled() {
        reloadModule.cancel();
        swapModule.cancel();
        aimModule.cancel();

        if (swapModule.isSwapped())
            swapModule.swap();
    }

    @Override
    public int getCapacity() {
        return JagerWeaponInfo.CAPACITY;
    }

    @Override
    @NonNull
    public Timespan getReloadDuration() {
        return JagerWeaponInfo.RELOAD_DURATION;
    }

    @Override
    public boolean canReload() {
        return Reloadable.super.canReload() || subweapon.getReloadModule().getRemainingAmmo() < subweapon.getCapacity();
    }

    @Override
    public void onAmmoEmpty() {
        reloadModule.reload();
    }

    @Override
    public void onReloadTick(long i) {
        JagerWeaponInfo.Effects.RELOAD.apply(i).play(combatUser.getLocation());
    }

    @Override
    public void onReloadFinished() {
        subweapon.getReloadModule().resetRemainingAmmo();
    }

    @Override
    @NonNull
    public Timespan getSwapDuration() {
        return JagerWeaponInfo.SWAP_DURATION;
    }

    @Override
    public void onSwapStart(boolean isSwapped) {
        setCooldown(JagerWeaponInfo.SWAP_DURATION);
        combatUser.setGlobalCooldown(JagerWeaponInfo.SWAP_DURATION);
    }

    @Override
    public void onSwapFinished(boolean isSwapped) {
        // 미사용
    }

    @Override
    @NonNull
    public ZoomLevel getZoomLevel() {
        return JagerWeaponInfo.Scope.ZOOM_LEVEL;
    }

    @Override
    public void onAimEnable() {
        combatUser.getMoveModule().addModifier(MODIFIER);
        JagerWeaponInfo.Effects.AIM_ON.play(combatUser.getLocation());
    }

    @Override
    public void onAimDisable() {
        combatUser.getMoveModule().removeModifier(MODIFIER);
        JagerWeaponInfo.Effects.AIM_OFF.play(combatUser.getLocation());
    }

    private final class JagerWeaponLProjectile extends Projectile<Damageable> {
        private JagerWeaponLProjectile() {
            super(JagerWeaponL.this, JagerWeaponInfo.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().maxDistance(JagerWeaponInfo.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            JagerWeaponInfo.Effects.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(10, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                JagerWeaponInfo.Effects.BULLET_TRAIL.play(loc);
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
                    JagerT1.addValue(target, JagerWeaponInfo.FREEZE);

                return false;
            };
        }
    }
}
