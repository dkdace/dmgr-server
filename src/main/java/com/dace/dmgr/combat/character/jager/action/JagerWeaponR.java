package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.Hitscan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.ReloadModule;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.Arrays;
import java.util.List;

public final class JagerWeaponR extends Weapon implements Reloadable {
    /** 주무기 객체 */
    private final JagerWeaponL mainWeapon;
    /** 재장전 모듈 객체 */
    private final ReloadModule reloadModule;

    /** 주무기 객체 */

    public JagerWeaponR(CombatUser combatUser, JagerWeaponL mainWeapon) {
        super(combatUser, JagerWeaponInfo.getInstance());
        reloadModule = new ReloadModule(this);
        this.mainWeapon = mainWeapon;
    }

    @Override
    public List<ActionKey> getDefaultActionKeys() {
        return Arrays.asList(ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK, ActionKey.DROP);
    }

    @Override
    public long getDefaultCooldown() {
        return JagerWeaponInfo.SCOPE.COOLDOWN;
    }

    @Override
    public int getRemainingAmmo() {
        return reloadModule.getRemainingAmmo();
    }

    @Override
    public void setRemainingAmmo(int remainingAmmo) {
        reloadModule.setRemainingAmmo(remainingAmmo);
    }

    @Override
    public boolean isReloading() {
        return reloadModule.isReloading();
    }

    @Override
    public void cancelReloading() {
        reloadModule.setReloading(false);
    }

    @Override
    public int getCapacity() {
        return JagerWeaponInfo.SCOPE.CAPACITY;
    }

    @Override
    public long getReloadDuration() {
        return 0;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && mainWeapon.canUse();
    }

    @Override
    public void onUse(ActionKey actionKey) {
        switch (actionKey) {
            case LEFT_CLICK: {
                if (getRemainingAmmo() == 0) {
                    reload();
                    return;
                }

                CooldownManager.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);
                Location location = combatUser.getEntity().getLocation();

                SoundUtil.play("random.gun2.psg_1_1", location, 3.5F, 1F);
                SoundUtil.play("random.gun2.m16_1", location, 3.5F, 1F);
                SoundUtil.play("random.gun.reverb", location, 5.5F, 0.95F);

                CombatUtil.setRecoil(combatUser, JagerWeaponInfo.SCOPE.RECOIL.UP, JagerWeaponInfo.SCOPE.RECOIL.SIDE,
                        JagerWeaponInfo.SCOPE.RECOIL.UP_SPREAD, JagerWeaponInfo.SCOPE.RECOIL.SIDE_SPREAD, 2, 1F);
                setCooldown();
                reloadModule.consume(1);

                new Hitscan(combatUser) {
                    @Override
                    public void trail(Location location) {
                        Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.2, 0);
                        ParticleUtil.play(Particle.CRIT, trailLoc, 1, 0, 0, 0, 0);
                    }

                    @Override
                    public void onHitEntity(Location location, CombatEntity<?> target, boolean isCrit) {
                        target.damage(combatUser, JagerWeaponInfo.SCOPE.DAMAGE, "", isCrit, true);
                    }
                }.shoot(0F);

                break;
            }
            case RIGHT_CLICK: {
                mainWeapon.aim();
                mainWeapon.swap();

                break;
            }
            case DROP: {
                reload();

                break;
            }
        }
    }

    @Override
    public void reload() {
        mainWeapon.reload();
    }
}
