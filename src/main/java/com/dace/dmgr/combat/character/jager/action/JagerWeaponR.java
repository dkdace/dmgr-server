package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.GunHitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.*;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;

public final class JagerWeaponR extends AbstractWeapon implements Reloadable {
    /** 주무기 객체 */
    private final JagerWeaponL mainWeapon;
    /** 재장전 모듈 */
    @Getter
    @NonNull
    private final ReloadModule reloadModule;

    JagerWeaponR(@NonNull CombatUser combatUser, @NonNull JagerWeaponL mainWeapon) {
        super(combatUser, JagerWeaponInfo.getInstance());
        this.mainWeapon = mainWeapon;
        reloadModule = new ReloadModule(this, JagerWeaponInfo.SCOPE.CAPACITY, 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK, ActionKey.DROP};
    }

    @Override
    public long getDefaultCooldown() {
        return JagerWeaponInfo.SCOPE.COOLDOWN;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && mainWeapon.canUse();
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
                CooldownUtil.setCooldown(combatUser, CombatUser.Cooldown.WEAPON_NO_SPRINT.getId(), CombatUser.Cooldown.WEAPON_NO_SPRINT.getDuration());

                new JagerWeaponRHitscan().shoot();
                reloadModule.consume(1);

                SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_WEAPON_USE_SCOPE, combatUser.getEntity().getLocation());
                CombatUtil.setRecoil(combatUser, JagerWeaponInfo.SCOPE.RECOIL.UP, JagerWeaponInfo.SCOPE.RECOIL.SIDE,
                        JagerWeaponInfo.SCOPE.RECOIL.UP_SPREAD, JagerWeaponInfo.SCOPE.RECOIL.SIDE_SPREAD, 2, 1);

                break;
            }
            case RIGHT_CLICK: {
                onCancelled();

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
        super.onCancelled();

        mainWeapon.getAimModule().setAiming(false);
        mainWeapon.getReloadModule().setReloading(false);
        mainWeapon.getSwapModule().setSwapping(false);
        mainWeapon.getSwapModule().swap();
    }

    @Override
    public boolean canReload() {
        return false;
    }

    @Override
    public void onAmmoEmpty() {
        if (reloadModule.isReloading())
            return;

        onCancelled();
        mainWeapon.getReloadModule().reload();
    }

    @Override
    public void onReloadTick(long i) {
        // 미사용
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    private final class JagerWeaponRHitscan extends GunHitscan {
        private double distance = 0;

        private JagerWeaponRHitscan() {
            super(combatUser, HitscanOption.builder().trailInterval(12).condition(combatUser::isEnemy).build());
        }

        @Override
        protected boolean onInterval() {
            distance += getVelocity().length();
            return super.onInterval();
        }

        @Override
        protected void trail() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0, -0.2, 0);
            ParticleUtil.play(Particle.CRIT, loc, 1, 0, 0, 0, 0);
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            int damage = CombatUtil.getDistantDamage(JagerWeaponInfo.SCOPE.DAMAGE, distance, JagerWeaponInfo.SCOPE.DAMAGE_WEAKENING_DISTANCE, true);
            target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, getLocation(), isCrit, true);

            return false;
        }
    }
}
