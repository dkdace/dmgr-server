package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.GunHitscan;
import com.dace.dmgr.combat.HitscanOption;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.WeaponBase;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.damageable.Damageable;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

@Getter
@Setter
public final class ArkaceWeapon extends WeaponBase implements Reloadable {
    /** 남은 탄약 수 */
    private int remainingAmmo = getCapacity();
    /** 재장전 상태 */
    private boolean reloading = false;

    public ArkaceWeapon(CombatUser combatUser) {
        super(combatUser, ArkaceWeaponInfo.getInstance());
    }

    @Override
    public ActionKey[] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.CS_PRE_USE, ActionKey.CS_USE, ActionKey.DROP};
    }

    @Override
    public long getDefaultCooldown() {
        return ArkaceWeaponInfo.COOLDOWN;
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
            case CS_PRE_USE: {
                if (getRemainingAmmo() == 0) {
                    reload();
                    return;
                }
                if (combatUser.getSkill(ArkaceP1Info.getInstance()).isDurationFinished())
                    return;

                setCooldown(4);
                CooldownManager.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);

                break;
            }
            case CS_USE: {
                CooldownManager.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);
                boolean isUlt = !combatUser.getSkill(ArkaceUltInfo.getInstance()).isDurationFinished();

                new ArkaceWeaponHitscan(isUlt).shoot(combatUser.getBulletSpread());

                Location loc = combatUser.getEntity().getLocation();
                if (isUlt)
                    playUltShootSound(loc);
                else {
                    CombatUtil.setRecoil(combatUser, ArkaceWeaponInfo.RECOIL.UP, ArkaceWeaponInfo.RECOIL.SIDE, ArkaceWeaponInfo.RECOIL.UP_SPREAD,
                            ArkaceWeaponInfo.RECOIL.SIDE_SPREAD, 2, 2F);
                    CombatUtil.setBulletSpread(combatUser, ArkaceWeaponInfo.SPREAD.INCREMENT, ArkaceWeaponInfo.SPREAD.RECOVERY, ArkaceWeaponInfo.SPREAD.MAX);
                    consume(1);
                    playShootSound(loc);
                }

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
     * @param location 발사 위치
     */
    private void playShootSound(Location location) {
        SoundUtil.play("random.gun2.scarlight_1", location, 3F, 1F);
        SoundUtil.play("random.gun_reverb", location, 5F, 1.2F);
    }

    /**
     * 발사 시 효과음을 재생한다. (궁극기)
     *
     * @param location 발사 위치
     */
    private void playUltShootSound(Location location) {
        SoundUtil.play("new.block.beacon.deactivate", location, 4F, 2F);
        SoundUtil.play("random.energy", location, 4F, 1.6F);
        SoundUtil.play("random.gun_reverb", location, 5F, 1.2F);
    }

    @Override
    public void onReloadTick(int i) {
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
    }

    @Override
    public void onReloadFinished() {
    }

    private class ArkaceWeaponHitscan extends GunHitscan {
        private final boolean isUlt;

        public ArkaceWeaponHitscan(boolean isUlt) {
            super(combatUser, HitscanOption.builder().condition(combatUser::isEnemy).build());
            this.isUlt = isUlt;
        }

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
        public boolean onHitEntity(Location location, Vector direction, Damageable target, boolean isCrit) {
            if (isUlt)
                target.damage(combatUser, ArkaceWeaponInfo.DAMAGE, DamageType.NORMAL, isCrit, false);
            else {
                int damage = CombatUtil.getDistantDamage(combatUser.getEntity().getLocation(), location, ArkaceWeaponInfo.DAMAGE,
                        ArkaceWeaponInfo.DAMAGE_DISTANCE, true);
                target.damage(combatUser, damage, DamageType.NORMAL, isCrit, true);
            }

            return false;
        }
    }
}
