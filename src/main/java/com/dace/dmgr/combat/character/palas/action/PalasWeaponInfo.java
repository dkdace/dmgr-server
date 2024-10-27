package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.weapon.Aimable;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;

public final class PalasWeaponInfo extends WeaponInfo<PalasWeapon> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.4 * 20);
    /** 사용 후 쿨타임 (tick) */
    public static final long ACTION_COOLDOWN = (long) (0.4 * 20);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 치유량 */
    public static final int HEAL = 250;
    /** 치유 투사체 크기 (단위: 블록) */
    public static final double HEAL_SIZE = 0.2;
    /** 탄퍼짐 */
    public static final double SPREAD = 5.0;
    /** 달리기 탄퍼짐 배수 */
    public static final double SPREAD_SPRINT_MULTIPLIER = 2.5;
    /** 장탄수 */
    public static final int CAPACITY = 10;
    /** 재장전 시간 (tick) */
    public static final long RELOAD_DURATION = (long) (2.2 * 20);
    /** 조준 시간 (tick) */
    public static final long AIM_DURATION = (long) (0.25 * 20);
    /** 조준 시 이동속도 감소량 */
    public static final int AIM_SLOW = 30;
    /** 확대 레벨 */
    public static final Aimable.ZoomLevel ZOOM_LEVEL = Aimable.ZoomLevel.L3;
    @Getter
    private static final PalasWeaponInfo instance = new PalasWeaponInfo();

    private PalasWeaponInfo() {
        super(PalasWeapon.class, RESOURCE.DEFAULT, "RQ-07",
                "",
                "§f▍ 생체탄을 발사하는 볼트액션 소총입니다.",
                "§f▍ §7사격§f하여 아군을 §a" + TextIcon.HEAL + " 치유§f하거나 적에게",
                "§f▍ §c" + TextIcon.DAMAGE + " 피해§f를 입힙니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.ATTACK_SPEED, (COOLDOWN + ACTION_COOLDOWN) / 20.0),
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§a{0}§f {1}", TextIcon.HEAL, HEAL),
                MessageFormat.format("§f{0} {1}발", TextIcon.CAPACITY, CAPACITY),
                "",
                "§7§l[좌클릭] §f사격 §7§l[우클릭] §f정조준",
                "§7§l[Q] §f재장전");
    }

    /**
     * 반동 정보.
     */
    @UtilityClass
    public static class RECOIL {
        /** 수직 반동 */
        public static final double UP = 2.5;
        /** 수평 반동 */
        public static final double SIDE = 0;
        /** 수직 반동 분산도 */
        public static final double UP_SPREAD = 0.15;
        /** 수평 반동 분산도 */
        public static final double SIDE_SPREAD = 0.2;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 15;
    }
}
