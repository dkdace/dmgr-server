package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;

public final class ArkaceWeaponInfo extends WeaponInfo<ArkaceWeapon> {
    /** 연사속도 */
    public static final FullAuto.FireRate FIRE_RATE = FullAuto.FireRate.RPM_600;
    /** 피해량 */
    public static final int DAMAGE = 75;
    /** 피해량 감소 시작 거리 (단위: 블록) */
    public static final int DAMAGE_WEAKENING_DISTANCE = 25;
    /** 장탄수 */
    public static final int CAPACITY = 30;
    /** 재장전 시간 (tick) */
    public static final long RELOAD_DURATION = (long) (1.5 * 20);
    /** 달리기 중 시전 시간 (tick) */
    public static final long SPRINT_READY_DURATION = (long) (0.25 * 20);
    @Getter
    private static final ArkaceWeaponInfo instance = new ArkaceWeaponInfo();

    private ArkaceWeaponInfo() {
        super(ArkaceWeapon.class, RESOURCE.DEFAULT, "HLN-12",
                "",
                "§f▍ 뛰어난 안정성을 가진 전자동 돌격소총입니다.",
                "§f▍ §7사격§f하여 §c" + TextIcon.DAMAGE + " 피해§f를 입힙니다.",
                "",
                MessageFormat.format("§c{0}§f {1} ~ {2} ({3}m~{4}m)",
                        TextIcon.DAMAGE, DAMAGE, DAMAGE / 2, DAMAGE_WEAKENING_DISTANCE, DAMAGE_WEAKENING_DISTANCE * 2),
                MessageFormat.format("§c{0}§f 0.1초 (600/분)", TextIcon.ATTACK_SPEED),
                MessageFormat.format("§f{0} {1}발", TextIcon.CAPACITY, CAPACITY),
                "",
                "§7§l[우클릭] §f사격 §7§l[Q] §f재장전");
    }

    /**
     * 반동 정보.
     */
    @UtilityClass
    public static class RECOIL {
        /** 수직 반동 */
        public static final double UP = 0.6;
        /** 수평 반동 */
        public static final double SIDE = 0.04;
        /** 수직 반동 분산도 */
        public static final double UP_SPREAD = 0.1;
        /** 수평 반동 분산도 */
        public static final double SIDE_SPREAD = 0.06;
    }

    /**
     * 탄퍼짐 정보.
     */
    @UtilityClass
    public static class SPREAD {
        /** 달리기 탄퍼짐 배수 */
        public static final double SPRINT_MULTIPLIER = 2;
        /** 탄퍼짐 증가량 */
        public static final double INCREMENT = 0.3;
        /** 탄퍼짐 시작 시점 */
        public static final int START = 5;
        /** 탄퍼짐 최대 시점 */
        public static final int MAX = 20;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 1;
        /** 달리기 */
        public static final short SPRINT = DEFAULT + 1000;
    }
}
