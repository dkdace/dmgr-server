package com.dace.dmgr.combat.character.arkace.action;

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
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

import java.util.Arrays;
import java.util.List;

public class ArkaceWeapon extends Weapon implements Reloadable {
    /** 재장전 모듈 객체 */
    private final ReloadModule reloadModule;

    public ArkaceWeapon(CombatUser combatUser) {
        super(combatUser, ArkaceWeaponInfo.getInstance());
        reloadModule = new ReloadModule(this);
    }

    @Override
    public List<ActionKey> getDefaultActionKeys() {
        return Arrays.asList(ActionKey.CS_PRE_USE, ActionKey.CS_USE, ActionKey.DROP);
    }

    @Override
    public long getDefaultCooldown() {
        return ArkaceWeaponInfo.COOLDOWN;
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
        return ArkaceWeaponInfo.CAPACITY;
    }

    @Override
    public long getReloadDuration() {
        return ArkaceWeaponInfo.RELOAD_DURATION;
    }

    @Override
    public void onUse(ActionKey actionKey) {
        switch (actionKey) {
            case CS_PRE_USE:
                if (getRemainingAmmo() == 0) {
                    reload();
                    return;
                }
                if (!combatUser.getSkill(ArkaceP1Info.getInstance()).isUsing())
                    return;

                setCooldown(4);
                CooldownManager.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);

                break;
            case CS_USE:
                CooldownManager.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);
                boolean isUlt = combatUser.getSkill(ArkaceUltInfo.getInstance()).isUsing();
                Location location = combatUser.getEntity().getLocation();

                if (isUlt) {
                    SoundUtil.play("new.block.beacon.deactivate", location, 4F, 2F);
                    SoundUtil.play("random.energy", location, 4F, 1.6F);
                    SoundUtil.play("random.gun_reverb", location, 5F, 1.2F);
                    combatUser.addBulletSpread(1, 0);
                } else {
                    SoundUtil.play("random.gun2.scarlight_1", location, 3F, 1F);
                    SoundUtil.play("random.gun_reverb", location, 5F, 1.2F);
                    CombatUtil.sendRecoil(combatUser, ArkaceWeaponInfo.RECOIL.UP, ArkaceWeaponInfo.RECOIL.SIDE, ArkaceWeaponInfo.RECOIL.UP_SPREAD,
                            ArkaceWeaponInfo.RECOIL.SIDE_SPREAD, 2, 2F);
                    CombatUtil.applyBulletSpread(combatUser, ArkaceWeaponInfo.SPREAD.INCREMENT, ArkaceWeaponInfo.SPREAD.RECOVERY, ArkaceWeaponInfo.SPREAD.MAX);
                    reloadModule.consume(1);
                }

                new Hitscan(combatUser, 7) {
                    @Override
                    public void trail(Location location) {
                        Location trailLoc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                        if (isUlt)
                            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc, 1,
                                    0, 0, 0, 0, 230, 255);
                        else
                            ParticleUtil.play(Particle.CRIT, trailLoc, 1, 0, 0, 0, 0);
                    }

                    @Override
                    public void onHitEntity(Location location, CombatEntity<?> target, boolean isCrit) {
                        combatUser.attack(target, ArkaceWeaponInfo.DAMAGE, "", isCrit, true);
                    }
                }.shoot(combatUser.getBulletSpread());

                break;
            case DROP:
                reload();

                break;
        }
    }

    @Override
    public void reload() {
        if (isReloading())
            return;

        reloadModule.reload();

        new TaskTimer(1, ArkaceWeaponInfo.RELOAD_DURATION) {
            @Override
            public boolean run(int i) {
                if (!isReloading())
                    return false;

                CooldownManager.setCooldown(combatUser, Cooldown.NO_SPRINT, 3);

                switch (i) {
                    case 3:
                        SoundUtil.play(Sound.BLOCK_PISTON_CONTRACT, combatUser.getEntity().getLocation(), 0.6F, 1.6F);
                        break;
                    case 4:
                        SoundUtil.play(Sound.ENTITY_VILLAGER_NO, combatUser.getEntity().getLocation(), 0.6F, 1.9F);
                        break;
                    case 18:
                        SoundUtil.play(Sound.ENTITY_PLAYER_HURT, combatUser.getEntity().getLocation(), 0.6F, 0.5F);
                        break;
                    case 19:
                        SoundUtil.play(Sound.ITEM_FLINTANDSTEEL_USE, combatUser.getEntity().getLocation(), 0.6F, 1F);
                        break;
                    case 20:
                        SoundUtil.play(Sound.ENTITY_VILLAGER_YES, combatUser.getEntity().getLocation(), 0.6F, 1.8F);
                        break;
                    case 26:
                        SoundUtil.play(Sound.ENTITY_WOLF_SHAKE, combatUser.getEntity().getLocation(), 0.6F, 1.7F);
                        break;
                    case 27:
                        SoundUtil.play(Sound.BLOCK_IRON_DOOR_OPEN, combatUser.getEntity().getLocation(), 0.6F, 1.8F);
                        break;
                }

                return true;
            }
        };
    }
}
