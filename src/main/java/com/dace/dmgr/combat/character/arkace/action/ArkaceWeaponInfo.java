package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.TextIcon;
import lombok.Getter;

public final class ArkaceWeaponInfo extends WeaponInfo {
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
        super(RESOURCE.DEFAULT, "HLN-12",
                "",
                "§f뛰어난 안정성을 가진 전자동 돌격소총입니다.",
                "§7사격§f하여 §c" + TextIcon.DAMAGE + " 피해§f를 입힙니다.",
                "",
                "§c" + TextIcon.DAMAGE + "§f " + DAMAGE + " (" + DAMAGE_DISTANCE + "m) - " + DAMAGE / 2 + " (" + DAMAGE_DISTANCE * 2 + "m)",
                "§c" + TextIcon.ATTACK_SPEED + "§f 0.1초",
                "§f" + TextIcon.CAPACITY + "§f 30발",
                "",
                "§7§l[우클릭] §f사격 §7§l[Q] §f재장전");
    }

    @Override
    public ArkaceWeapon createWeapon(CombatUser combatUser) {
        return new ArkaceWeapon(combatUser);
    }

    /**
     * 반동 정보.
     */
    public interface RECOIL {
        /** 수직 반동 */
        float UP = 0.6F;
        /** 수평 반동 */
        float SIDE = 0.04F;
        /** 수직 반동 분산도 */
        float UP_SPREAD = 0.1F;
        /** 수평 반동 분산도 */
        float SIDE_SPREAD = 0.06F;
    }

    /**
     * 탄퍼짐 정보.
     */
    public interface SPREAD {
        /** 탄퍼짐 증가량 */
        float INCREMENT = 0.3F;
        /** 탄퍼짐 회복량 */
        float RECOVERY = 2F;
        /** 탄퍼짐 최대치 */
        float MAX = 4.6F;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    public interface RESOURCE {
        /** 기본 */
        short DEFAULT = 1;
        /** 달리기 */
        short SPRINT = DEFAULT + 1000;
    }
}
