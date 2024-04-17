package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.GunHitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
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

    public JagerWeaponR(@NonNull CombatUser combatUser, @NonNull JagerWeaponL mainWeapon) {
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
                    reload();
                    return;
                }

                new JagerWeaponRHitscan().shoot();
                SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_WEAPON_USE_SCOPE, combatUser.getEntity().getLocation());

                CooldownUtil.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);
                CombatUtil.setRecoil(combatUser, JagerWeaponInfo.SCOPE.RECOIL.UP, JagerWeaponInfo.SCOPE.RECOIL.SIDE,
                        JagerWeaponInfo.SCOPE.RECOIL.UP_SPREAD, JagerWeaponInfo.SCOPE.RECOIL.SIDE_SPREAD, 2, 1);
                setCooldown();
                reloadModule.consume(1);

                break;
            }
            case RIGHT_CLICK: {
                mainWeapon.getAimModule().toggleAim();
                mainWeapon.getSwapModule().swap();

                break;
            }
            case DROP: {
                reload();

                break;
            }
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        mainWeapon.getAimModule().toggleAim();
        mainWeapon.getSwapModule().swap();
        reloadModule.setReloading(false);
    }

    private void reload() {
        if (mainWeapon.getAimModule().isAiming()) {
            mainWeapon.getAimModule().toggleAim();
            mainWeapon.getSwapModule().swap();

            TaskUtil.addTask(taskRunner, new DelayTask(() -> mainWeapon.getReloadModule().reload(), JagerWeaponInfo.SWAP_DURATION));
        }
    }

    @Override
    public boolean canReload() {
        return false;
    }

    @Override
    public void onAmmoEmpty() {
        reload();
    }

    @Override
    public void onReloadTick(long i) {
        // 미사용
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    private class JagerWeaponRHitscan extends GunHitscan {
        private JagerWeaponRHitscan() {
            super(JagerWeaponR.this.combatUser, HitscanOption.builder().trailInterval(12).condition(JagerWeaponR.this.combatUser::isEnemy).build());
        }

        @Override
        protected void trail() {
            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.2, 0);
            ParticleUtil.play(Particle.CRIT, trailLoc, 1, 0, 0, 0, 0);
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            int damage = CombatUtil.getDistantDamage(combatUser.getEntity().getLocation(), location, JagerWeaponInfo.SCOPE.DAMAGE,
                    JagerWeaponInfo.SCOPE.DAMAGE_DISTANCE, true);
            target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, location, isCrit, true);
            return false;
        }
    }
}
