package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.weapon.Aimable;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

public final class PalasWeaponInfo extends WeaponInfo<PalasWeapon> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.4 * 20);
    /** 사용 후 쿨타임 (tick) */
    public static final long ACTION_COOLDOWN = (long) (0.4 * 20);
    /** 피해량 */
    public static final int DAMAGE = 300;
    /** 치유량 */
    public static final int HEAL = 300;
    /** 치유 투사체 크기 (단위: 블록) */
    public static final double HEAL_SIZE = 0.2;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 30;
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
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("생체탄을 발사하는 볼트액션 소총입니다. " +
                                "사격하여 아군을 <:HEAL:치유>하거나 적에게 <:DAMAGE:피해>를 입힙니다. " +
                                "정조준 시 사거리 제한이 사라집니다.")
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, ChatColor.WHITE, (COOLDOWN + ACTION_COOLDOWN) / 20.0)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.HEAL, HEAL)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, CAPACITY)
                        .addActionKeyInfo("사격", ActionKey.LEFT_CLICK)
                        .addActionKeyInfo("정조준", ActionKey.RIGHT_CLICK)
                        .addActionKeyInfo("재장전", ActionKey.DROP)
                        .build()
                )
        );
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
