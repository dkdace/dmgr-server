package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.weapon.Aimable;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

public final class JagerWeaponInfo extends WeaponInfo<JagerWeaponL> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.25 * 20);
    /** 피해량 */
    public static final int DAMAGE = 100;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 30;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 100;
    /** 탄퍼짐 */
    public static final double SPREAD = 2.5;
    /** 달리기 탄퍼짐 배수 */
    public static final double SPREAD_SPRINT_MULTIPLIER = 2.5;
    /** 빙결량 */
    public static final int FREEZE = 15;
    /** 장탄수 */
    public static final int CAPACITY = 10;
    /** 재장전 시간 (tick) */
    public static final long RELOAD_DURATION = 2 * 20L;
    /** 무기 교체 시간 (tick) */
    public static final long SWAP_DURATION = (long) (0.25 * 20);
    /** 조준 시 이동속도 감소량 */
    public static final int AIM_SLOW = 30;
    @Getter
    private static final JagerWeaponInfo instance = new JagerWeaponInfo();

    private JagerWeaponInfo() {
        super(JagerWeaponL.class, RESOURCE.DEFAULT, "MK.73 ELNR",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("두 개의 탄창을 가진 특수 소총으로, <3::냉각탄> 및 정조준하여 <3::저격탄>을 사격할 수 있습니다.")
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, COOLDOWN / 20.0)
                        .addActionKeyInfo("사격", ActionKey.LEFT_CLICK)
                        .addActionKeyInfo("정조준", ActionKey.RIGHT_CLICK)
                        .addActionKeyInfo("재장전", ActionKey.DROP)
                        .build(),
                        new ActionInfoLore.NamedSection("냉각탄", ActionInfoLore.Section
                                .builder("냉각탄을 사격하여 <:DAMAGE:피해>를 입히고 <5:WALK_SPEED_DECREASE:> <d::빙결>시킵니다.")
                                .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                                .addValueInfo(TextIcon.WALK_SPEED_DECREASE, ChatColor.DARK_PURPLE, FREEZE)
                                .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                                .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, CAPACITY)
                                .build()
                        ),
                        new ActionInfoLore.NamedSection("저격탄", ActionInfoLore.Section
                                .builder("저격탄을 사격하여 <:DAMAGE:피해>를 입힙니다.")
                                .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_WITH_DISTANCE,
                                        SCOPE.DAMAGE, SCOPE.DAMAGE / 2, SCOPE.DAMAGE_WEAKENING_DISTANCE, SCOPE.DAMAGE_WEAKENING_DISTANCE * 2)
                                .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, SCOPE.CAPACITY)
                                .build()
                        )
                )
        );
    }

    /**
     * 정조준 상태의 정보.
     */
    @UtilityClass
    public static class SCOPE {
        /** 피해량 */
        public static final int DAMAGE = 240;
        /** 피해량 감소 시작 거리 (단위: 블록) */
        public static final int DAMAGE_WEAKENING_DISTANCE = 30;
        /** 장탄수 */
        public static final int CAPACITY = 7;
        /** 확대 레벨 */
        public static final Aimable.ZoomLevel ZOOM_LEVEL = Aimable.ZoomLevel.L4;

        /**
         * 반동 정보.
         */
        @UtilityClass
        public static class RECOIL {
            /** 수직 반동 */
            public static final double UP = 2.8;
            /** 수평 반동 */
            public static final double SIDE = 0;
            /** 수직 반동 분산도 */
            public static final double UP_SPREAD = 0.25;
            /** 수평 반동 분산도 */
            public static final double SIDE_SPREAD = 0.3;
        }
    }

    /**
     * 반동 정보.
     */
    @UtilityClass
    public static class RECOIL {
        /** 수직 반동 */
        public static final double UP = 0.8;
        /** 수평 반동 */
        public static final double SIDE = 0;
        /** 수직 반동 분산도 */
        public static final double UP_SPREAD = 0.1;
        /** 수평 반동 분산도 */
        public static final double SIDE_SPREAD = 0.05;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 2;
    }
}
