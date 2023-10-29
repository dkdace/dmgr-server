package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.GunHitscan;
import com.dace.dmgr.combat.HitscanOption;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.ActionModule;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.WeaponBase;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public final class JagerWeaponR extends WeaponBase implements Reloadable {
    /** 주무기 객체 */
    private final JagerWeaponL mainWeapon;
    /** 재장전 모듈 */
    @Getter
    private final ReloadModule reloadModule;

    public JagerWeaponR(CombatUser combatUser, JagerWeaponL mainWeapon) {
        super(combatUser, JagerWeaponInfo.getInstance());
        this.mainWeapon = mainWeapon;
        reloadModule = new ReloadModule(this, JagerWeaponInfo.SCOPE.CAPACITY, 0);
    }

    @Override
    public ActionModule[] getModules() {
        return new ActionModule[]{reloadModule};
    }

    @Override
    public ActionKey[] getDefaultActionKeys() {
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
    public void onUse(ActionKey actionKey) {
        switch (actionKey) {
            case LEFT_CLICK: {
                if (reloadModule.getRemainingAmmo() == 0) {
                    reload();
                    return;
                }

                new JagerWeaponProjectile().shoot(0F);

                CooldownManager.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);
                CombatUtil.setRecoil(combatUser, JagerWeaponInfo.SCOPE.RECOIL.UP, JagerWeaponInfo.SCOPE.RECOIL.SIDE,
                        JagerWeaponInfo.SCOPE.RECOIL.UP_SPREAD, JagerWeaponInfo.SCOPE.RECOIL.SIDE_SPREAD, 2, 1F);
                setCooldown();
                reloadModule.consume(1);
                playShootSound(combatUser.getEntity().getLocation());

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

    /**
     * 발사 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playShootSound(Location location) {
        SoundUtil.play("random.gun2.psg_1_1", location, 3.5F, 1F);
        SoundUtil.play("random.gun2.m16_1", location, 3.5F, 1F);
        SoundUtil.play("random.gun.reverb", location, 5.5F, 0.95F);
    }

    private void reload() {
        if (mainWeapon.getAimModule().isAiming()) {
            mainWeapon.getAimModule().toggleAim();
            mainWeapon.getSwapModule().swap();

            TaskManager.addTask(this, new TaskTimer(1, JagerWeaponInfo.SWAP_DURATION) {
                @Override
                public boolean onTimerTick(int i) {
                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    mainWeapon.getReloadModule().reload();
                }
            });
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

    private class JagerWeaponProjectile extends GunHitscan {
        public JagerWeaponProjectile() {
            super(JagerWeaponR.this.combatUser, HitscanOption.builder().trailInterval(12).condition(JagerWeaponR.this.combatUser::isEnemy).build());
        }

        @Override
        public void trail(Location location) {
            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.2, 0);
            ParticleUtil.play(Particle.CRIT, trailLoc, 1, 0, 0, 0, 0);
        }

        @Override
        public boolean onHitEntity(Location location, Vector direction, Damageable target, boolean isCrit) {
            int damage = CombatUtil.getDistantDamage(combatUser.getEntity().getLocation(), location, JagerWeaponInfo.SCOPE.DAMAGE,
                    JagerWeaponInfo.SCOPE.DAMAGE_DISTANCE, true);
            target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, isCrit, true);
            return false;
        }
    }
}
