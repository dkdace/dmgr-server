package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.Combat;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.Hitscan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.Reloadable;
import com.dace.dmgr.combat.action.Weapon;
import com.dace.dmgr.combat.action.WeaponController;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.TextIcon;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class ArkaceWeapon extends Weapon implements Reloadable {
    /** 피해량 */
    public static final int DAMAGE = 75;
    /** 피해량 감소 시작 거리 */
    public static final int DAMAGE_DISTANCE = 25;
    /** 쿨타임 */
    public static final long COOLDOWN = (long) (0.1 * 20);
    /** 장탄수 */
    public static final int CAPACITY = 30;
    /** 재장전 시간 */
    public static final long RELOAD_DURATION = (long) (1.5 * 20);
    private static final ArkaceWeapon instance = new ArkaceWeapon();

    public ArkaceWeapon() {
        super("HLN-12", ItemBuilder.fromCSItem("HLN-12").setLore(
                "",
                "§f뛰어난 안정성을 가진 전자동 돌격소총입니다.",
                "§7사격§f하여 §c" + TextIcon.DAMAGE + " 피해§f를 입힙니다.",
                "",
                "§c" + TextIcon.DAMAGE + "§f " + DAMAGE + " (" + DAMAGE_DISTANCE + "m) - " + DAMAGE / 2 + " (" + DAMAGE_DISTANCE * 2 + "m)",
                "§c" + TextIcon.ATTACK_SPEED + "§f 0.1초",
                "§f" + TextIcon.CAPACITY + "§f 30발",
                "",
                "§7§l[우클릭] §f사격 §7§l[Q] §f재장전").build());
    }

    public static ArkaceWeapon getInstance() {
        return instance;
    }

    @Override
    public long getCooldown() {
        return COOLDOWN;
    }

    @Override
    public int getCapacity() {
        return CAPACITY;
    }

    @Override
    public long getReloadDuration() {
        return RELOAD_DURATION;
    }

    @Override
    public void use(CombatUser combatUser, WeaponController weaponController, ActionKey actionKey) {
        switch (actionKey) {
            case CS_PRE_USE:
                if (weaponController.getRemainingAmmo() == 0) {
                    reload(combatUser, weaponController);
                    return;
                }
                if (!combatUser.getSkillController(ArkaceP1.getInstance()).isUsing())
                    return;

                weaponController.setCooldown(4);
                CooldownManager.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);

                break;
            case CS_USE:
                CooldownManager.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);
                boolean isUlt = combatUser.getSkillController(ArkaceUlt.getInstance()).isUsing();
                Location location = combatUser.getEntity().getLocation();

                if (isUlt) {
                    SoundUtil.play("new.block.beacon.deactivate", location, 4F, 2F);
                    SoundUtil.play("random.energy", location, 4F, 1.6F);
                    SoundUtil.play("random.gun_reverb", location, 5F, 1.2F);
                    combatUser.addBulletSpread(1, 0);
                } else {
                    SoundUtil.play("random.gun2.scarlight_1", location, 3F, 1F);
                    SoundUtil.play("random.gun_reverb", location, 5F, 1.2F);
                    CombatUtil.sendRecoil(combatUser, RECOIL.UP, RECOIL.SIDE, RECOIL.UP_SPREAD, RECOIL.SIDE_SPREAD, 2, 2F);
                    CombatUtil.applyBulletSpread(combatUser, SPREAD.INCREMENT, SPREAD.RECOVERY, SPREAD.MAX);
                    weaponController.consume(1);
                }

                new Hitscan(combatUser, false, 7) {
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
                    public void onHitEntity(Location location, ICombatEntity target, boolean isCrit) {
                        Combat.attack(combatUser, target, DAMAGE, "", isCrit, true);
                    }
                }.shoot(combatUser.getBulletSpread());

                break;
            case DROP:
                reload(combatUser, weaponController);

                break;
        }
    }

    @Override
    public void reload(CombatUser combatUser, WeaponController weaponController) {
        if (weaponController.isReloading())
            return;

        weaponController.reload();

        new TaskTimer(1, RELOAD_DURATION) {
            @Override
            public boolean run(int i) {
                if (!weaponController.isReloading())
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

    /**
     * 반동 정보.
     */
    public static class RECOIL {
        /** 수직 반동 */
        static final float UP = 0.6F;
        /** 수평 반동 */
        static final float SIDE = 0.04F;
        /** 수직 반동 분산도 */
        static final float UP_SPREAD = 0.1F;
        /** 수평 반동 분산도 */
        static final float SIDE_SPREAD = 0.06F;
    }

    /**
     * 탄퍼짐 정보.
     */
    public static class SPREAD {
        /** 탄퍼짐 증가량 */
        static final float INCREMENT = 0.15F;
        /** 탄퍼짐 회복량 */
        static final float RECOVERY = 1F;
        /** 탄퍼짐 최대치 */
        static final float MAX = 2.3F;
    }
}
