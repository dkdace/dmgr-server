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
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
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
        return JagerWeaponInfo.COOLDOWN;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return actionKey == ActionKey.DROP ? combatUser.isGlobalCooldownFinished() : super.canUse(actionKey);
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

                new JagerWeaponRHitscan().shoot();
                reloadModule.consume(1);

                Location loc = combatUser.getEntity().getLocation();
                SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_WEAPON_USE_SCOPE, loc);
                CombatUtil.setRecoil(combatUser, JagerWeaponInfo.SCOPE.RECOIL.UP, JagerWeaponInfo.SCOPE.RECOIL.SIDE,
                        JagerWeaponInfo.SCOPE.RECOIL.UP_SPREAD, JagerWeaponInfo.SCOPE.RECOIL.SIDE_SPREAD, 2, 1);
                TaskUtil.addTask(this, new DelayTask(() -> SoundUtil.playNamedSound(NamedSound.COMBAT_GUN_SHELL_DROP, loc,
                        1, -0.05), 8));

                break;
            }
            case RIGHT_CLICK: {
                setCooldown(2);
                onCancelled();

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

        mainWeapon.getAimModule().cancel();
        mainWeapon.getReloadModule().cancel();
        mainWeapon.getSwapModule().cancel();
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
        TaskUtil.addTask(taskRunner, new DelayTask(() -> mainWeapon.getReloadModule().reload(), getDefaultCooldown()));
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
        protected void onTrailInterval() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0, -0.2, 0);
            ParticleUtil.play(Particle.CRIT, loc, 1, 0, 0, 0, 0);
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            double damage = CombatUtil.getDistantDamage(JagerWeaponInfo.SCOPE.DAMAGE, distance, JagerWeaponInfo.SCOPE.DAMAGE_WEAKENING_DISTANCE);
            target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, getLocation(), isCrit, true);

            return false;
        }
    }
}
