package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.GradualSpreadModule;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.GunHitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.*;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

@Getter
public final class ArkaceWeapon extends AbstractWeapon implements Reloadable, FullAuto {
    /** 재장전 모듈 */
    @NonNull
    private final ReloadModule reloadModule;
    /** 연사 모듈 */
    @NonNull
    private final GradualSpreadModule fullAutoModule;

    public ArkaceWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceWeaponInfo.getInstance());
        reloadModule = new ReloadModule(this, ArkaceWeaponInfo.CAPACITY, ArkaceWeaponInfo.RELOAD_DURATION);
        fullAutoModule = new GradualSpreadModule(this, ActionKey.RIGHT_CLICK, FireRate.RPM_600, ArkaceWeaponInfo.SPREAD.INCREMENT,
                ArkaceWeaponInfo.SPREAD.START, ArkaceWeaponInfo.SPREAD.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.RIGHT_CLICK, ActionKey.DROP};
    }

    @Override
    public long getDefaultCooldown() {
        return 0;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case RIGHT_CLICK: {
                if (reloadModule.getRemainingAmmo() == 0) {
                    reloadModule.reload();
                    return;
                }
                if (!combatUser.getSkill(ArkaceP1Info.getInstance()).isDurationFinished()) {
                    setCooldown(4);
                    CooldownUtil.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);
                    return;
                }

                CooldownUtil.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);
                boolean isUlt = !combatUser.getSkill(ArkaceUltInfo.getInstance()).isDurationFinished();
                Location loc = combatUser.getEntity().getLocation();

                if (isUlt) {
                    new ArkaceWeaponHitscan(true).shoot(0);
                    playUltShootSound(loc);
                } else {
                    new ArkaceWeaponHitscan(false).shoot(fullAutoModule.increaseSpread());
                    CombatUtil.setRecoil(combatUser, ArkaceWeaponInfo.RECOIL.UP, ArkaceWeaponInfo.RECOIL.SIDE, ArkaceWeaponInfo.RECOIL.UP_SPREAD,
                            ArkaceWeaponInfo.RECOIL.SIDE_SPREAD, 2, 2);
                    reloadModule.consume(1);
                    playShootSound(loc);
                }

                break;
            }
            case DROP: {
                reloadModule.reload();

                break;
            }
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        reloadModule.setReloading(false);
    }

    /**
     * 발사 시 효과음을 재생한다.
     *
     * @param location 발사 위치
     */
    private void playShootSound(Location location) {
        SoundUtil.play("random.gun2.scarlight_1", location, 3, 1);
        SoundUtil.play("random.gun_reverb", location, 5, 1.2);
    }

    /**
     * 발사 시 효과음을 재생한다. (궁극기)
     *
     * @param location 발사 위치
     */
    private void playUltShootSound(Location location) {
        SoundUtil.play("new.block.beacon.deactivate", location, 4, 2);
        SoundUtil.play("random.energy", location, 4, 1.6);
        SoundUtil.play("random.gun_reverb", location, 5, 1.2);
    }

    @Override
    public boolean canReload() {
        return reloadModule.getRemainingAmmo() < ArkaceWeaponInfo.CAPACITY;
    }

    @Override
    public void onAmmoEmpty() {
        reloadModule.reload();
    }

    @Override
    public void onReloadTick(long i) {
        CooldownUtil.setCooldown(combatUser, Cooldown.NO_SPRINT, 3);

        switch ((int) i) {
            case 3:
                SoundUtil.play(Sound.BLOCK_PISTON_CONTRACT, combatUser.getEntity().getLocation(), 0.6, 1.6);
                break;
            case 4:
                SoundUtil.play(Sound.ENTITY_VILLAGER_NO, combatUser.getEntity().getLocation(), 0.6, 1.9);
                break;
            case 18:
                SoundUtil.play(Sound.ENTITY_PLAYER_HURT, combatUser.getEntity().getLocation(), 0.6, 0.5);
                break;
            case 19:
                SoundUtil.play(Sound.ITEM_FLINTANDSTEEL_USE, combatUser.getEntity().getLocation(), 0.6, 1);
                break;
            case 20:
                SoundUtil.play(Sound.ENTITY_VILLAGER_YES, combatUser.getEntity().getLocation(), 0.6, 1.8);
                break;
            case 26:
                SoundUtil.play(Sound.ENTITY_WOLF_SHAKE, combatUser.getEntity().getLocation(), 0.6, 1.7);
                break;
            case 27:
                SoundUtil.play(Sound.BLOCK_IRON_DOOR_OPEN, combatUser.getEntity().getLocation(), 0.6, 1.8);
                break;
        }
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    private class ArkaceWeaponHitscan extends GunHitscan {
        private final boolean isUlt;

        public ArkaceWeaponHitscan(boolean isUlt) {
            super(combatUser, HitscanOption.builder().condition(combatUser::isEnemy).build());
            this.isUlt = isUlt;
        }

        @Override
        public void trail(@NonNull Location location) {
            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
            if (isUlt)
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc, 1,
                        0, 0, 0, 0, 230, 255);
            else
                ParticleUtil.play(Particle.CRIT, trailLoc, 1, 0, 0, 0, 0);
        }

        @Override
        public boolean onHitEntity(@NonNull Location location, @NonNull Vector direction, @NonNull Damageable target, boolean isCrit) {
            if (isUlt)
                target.getDamageModule().damage(combatUser, ArkaceWeaponInfo.DAMAGE, DamageType.NORMAL, isCrit, false);
            else {
                int damage = CombatUtil.getDistantDamage(combatUser.getEntity().getLocation(), location, ArkaceWeaponInfo.DAMAGE,
                        ArkaceWeaponInfo.DAMAGE_DISTANCE, true);
                target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, isCrit, true);
            }

            return false;
        }
    }
}
