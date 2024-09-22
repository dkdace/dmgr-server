package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;

public final class InfernoWeaponInfo extends WeaponInfo<InfernoWeapon> {
    /** 초당 피해량 */
    public static final int DAMAGE_PER_SECOND = 150;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 40;
    /** 화염 지속 시간 (tick) */
    public static final long FIRE_DURATION = (long) (2.5 * 20);
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 7;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 0.8;
    /** 탄퍼짐 */
    public static final double SPREAD = 30;
    /** 장탄수 */
    public static final int CAPACITY = 200;
    /** 재장전 시간 (tick) */
    public static final long RELOAD_DURATION = (long) (2.5 * 20);
    @Getter
    private static final InfernoWeaponInfo instance = new InfernoWeaponInfo();

    private InfernoWeaponInfo() {
        super(InfernoWeapon.class, RESOURCE.DEFAULT, "파이어스톰",
                "",
                "§f▍ 근거리에 화염을 흩뿌리거나 §3화염탄§f을 발사할",
                "§f▍ 수 있는 화염방사기입니다.",
                "",
                MessageFormat.format("§f{0} {1}발", TextIcon.CAPACITY, CAPACITY),
                "",
                "§7§l[우클릭] §f방사 §7§l[좌클릭] §f화염탄",
                "",
                "§3[방사]",
                "",
                "§f▍ 근거리에 화염을 §7방사§f하여 §c" + TextIcon.DAMAGE + " 광역 피해§f와",
                "§f▍ §c" + TextIcon.FIRE + " 화염 피해§f를 입힙니다.",
                "",
                MessageFormat.format("§c{0}§f {1}/초", TextIcon.DAMAGE, DAMAGE_PER_SECOND),
                MessageFormat.format("§c{0}§f {1}초 / {2}/초", TextIcon.FIRE, FIRE_DURATION / 20.0, FIRE_DAMAGE_PER_SECOND),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.DISTANCE, DISTANCE),
                "",
                "§3[화염탄]",
                "",
                "§f▍ 폭발하는 화염 구체를 발사하여 §c" + TextIcon.DAMAGE + " 광역 피해",
                "§f▍ 와 §c" + TextIcon.FIRE + " 화염 피해§f를 입힙니다.",
                "",
                MessageFormat.format("§c{0}§f {1} ~ {2} (폭발)", TextIcon.DAMAGE, FIREBALL.DAMAGE_EXPLODE, FIREBALL.DAMAGE_EXPLODE / 2),
                MessageFormat.format("§c{0}§f {1} (직격)", TextIcon.DAMAGE, FIREBALL.DAMAGE_DIRECT),
                MessageFormat.format("§c{0}§f {1}초 ~ {2}초 / {3}/초", TextIcon.FIRE, FIRE_DURATION / 20.0, FIRE_DURATION / 2 / 20.0, FIRE_DAMAGE_PER_SECOND),
                MessageFormat.format("§c{0}§f {1}초", TextIcon.ATTACK_SPEED, FIREBALL.COOLDOWN / 20.0),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.DISTANCE, FIREBALL.DISTANCE),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, FIREBALL.RADIUS),
                MessageFormat.format("§f{0} -{1}", TextIcon.CAPACITY, FIREBALL.CAPACITY_CONSUME));
    }

    /**
     * 화염탄의 정보.
     */
    @UtilityClass
    public static class FIREBALL {
        /** 쿨타임 (tick) */
        public static final long COOLDOWN = 1 * 20;
        /** 피해량 (폭발) */
        public static final int DAMAGE_EXPLODE = 100;
        /** 피해량 (직격) */
        public static final int DAMAGE_DIRECT = 40;
        /** 사거리 (단위: 블록) */
        public static final int DISTANCE = 10;
        /** 투사체 속력 (단위: 블록/s) */
        public static final int VELOCITY = 30;
        /** 투사체 크기 (단위: 블록) */
        public static final double SIZE = 0.5;
        /** 피해 범위 (단위: 블록) */
        public static final double RADIUS = 2.5;
        /** 탄환 소모량 */
        public static final int CAPACITY_CONSUME = 40;
        /** 넉백 강도 */
        public static final double KNOCKBACK = 0.3;

        /**
         * 반동 정보.
         */
        @UtilityClass
        public static class RECOIL {
            /** 수직 반동 */
            public static final double UP = 5.0;
            /** 수평 반동 */
            public static final double SIDE = 0;
            /** 수직 반동 분산도 */
            public static final double UP_SPREAD = 1.0;
            /** 수평 반동 분산도 */
            public static final double SIDE_SPREAD = 0.8;
        }
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 12;
    }
}
