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
import com.dace.dmgr.util.*;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Sound;
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

    JagerWeaponL(@NonNull CombatUser combatUser) {
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
    public boolean canUse() {
        return super.canUse() && !((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).getConfirmModule().isChecking() &&
                combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
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

                double spread = JagerWeaponInfo.SPREAD;
                if (combatUser.getEntity().isSprinting())
                    spread *= JagerWeaponInfo.SPREAD_SPRINT_MULTIPLIER;

                Vector dir = VectorUtil.getSpreadedVector(combatUser.getEntity().getLocation().getDirection(), spread);
                new JagerWeaponLProjectile().shoot(dir);
                reloadModule.consume(1);

                SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_WEAPON_USE, combatUser.getEntity().getLocation());
                CombatUtil.setRecoil(combatUser, JagerWeaponInfo.RECOIL.UP, JagerWeaponInfo.RECOIL.SIDE, JagerWeaponInfo.RECOIL.UP_SPREAD,
                        JagerWeaponInfo.RECOIL.SIDE_SPREAD, 2, 1);

                break;
            }
            case RIGHT_CLICK: {
                onCancelled();
                aimModule.toggleAim();
                swapModule.swap();

                break;
            }
            case DROP: {
                onAmmoEmpty();

                break;
            }
        }
    }

    @Override
    public void onCancelled() {
        if (swapModule.getSwapState() == SwapState.SECONDARY)
            swapModule.getSubweapon().onCancelled();
        else {
            super.onCancelled();
            reloadModule.setReloading(false);
            swapModule.setSwapping(false);
            aimModule.setAiming(false);
        }
    }

    @Override
    public boolean canReload() {
        return reloadModule.getRemainingAmmo() < JagerWeaponInfo.CAPACITY ||
                swapModule.getSubweapon().getReloadModule().getRemainingAmmo() < JagerWeaponInfo.SCOPE.CAPACITY;
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
                SoundUtil.play(Sound.ENTITY_WOLF_HOWL, combatUser.getEntity().getLocation(), 0.6, 1.7);
                break;
            case 4:
                SoundUtil.play(Sound.BLOCK_FIRE_EXTINGUISH, combatUser.getEntity().getLocation(), 0.6, 1.2);
                break;
            case 6:
                SoundUtil.play(Sound.ITEM_FLINTANDSTEEL_USE, combatUser.getEntity().getLocation(), 0.6, 0.8);
                break;
            case 25:
                SoundUtil.play(Sound.ENTITY_PLAYER_HURT, combatUser.getEntity().getLocation(), 0.6, 0.5);
                break;
            case 27:
                SoundUtil.play(Sound.ENTITY_CAT_PURREOW, combatUser.getEntity().getLocation(), 0.6, 1.7);
                break;
            case 35:
                SoundUtil.play(Sound.ENTITY_WOLF_SHAKE, combatUser.getEntity().getLocation(), 0.6, 1.8);
                break;
            case 37:
                SoundUtil.play(Sound.BLOCK_IRON_DOOR_OPEN, combatUser.getEntity().getLocation(), 0.6, 1.7);
                break;
        }
    }

    @Override
    public void onReloadFinished() {
        swapModule.getSubweapon().getReloadModule().setRemainingAmmo(JagerWeaponInfo.SCOPE.CAPACITY);
    }

    @Override
    public void onSwapStart(@NonNull SwapState swapState) {
        Location location = combatUser.getEntity().getLocation();
        if (swapState == SwapState.PRIMARY)
            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_WEAPON_SWAP_OFF, location);
        else if (swapState == SwapState.SECONDARY)
            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_WEAPON_SWAP_ON, location);

        setCooldown(JagerWeaponInfo.SWAP_DURATION);
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
        protected void trail() {
            Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 1, 0, 0, 0, 137, 185, 240);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.getDamageModule().damage(this, JagerWeaponInfo.DAMAGE, DamageType.NORMAL, location, false, true))
                JagerT1.addFreezeValue(target, JagerWeaponInfo.FREEZE);

            return false;
        }
    }
}
