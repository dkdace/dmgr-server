package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.action.weapon.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.system.TextIcon;
import lombok.Getter;

public class ArkaceWeaponInfo extends WeaponInfo {
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
    @Getter
    private static final ArkaceWeaponInfo instance = new ArkaceWeaponInfo();

    public ArkaceWeaponInfo() {
        super("HLN-12", ItemBuilder.fromCSItem("HLN-12")
                .setLore(
                        "",
                        "§f뛰어난 안정성을 가진 전자동 돌격소총입니다.",
                        "§7사격§f하여 §c" + TextIcon.DAMAGE + " 피해§f를 입힙니다.",
                        "",
                        "§c" + TextIcon.DAMAGE + "§f " + DAMAGE + " (" + DAMAGE_DISTANCE + "m) - " + DAMAGE / 2 + " (" + DAMAGE_DISTANCE * 2 + "m)",
                        "§c" + TextIcon.ATTACK_SPEED + "§f 0.1초",
                        "§f" + TextIcon.CAPACITY + "§f 30발",
                        "",
                        "§7§l[우클릭] §f사격 §7§l[Q] §f재장전")
                .build());
    }

    @Override
    public Weapon createWeapon(CombatUser combatUser) {
        return new ArkaceWeapon(combatUser);
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