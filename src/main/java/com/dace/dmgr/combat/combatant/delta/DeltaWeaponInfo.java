package com.dace.dmgr.combat.combatant.delta;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import lombok.Getter;
import lombok.experimental.UtilityClass;

public final class DeltaWeaponInfo extends WeaponInfo<DeltaWeapon> {
    /** 기본 초당 피해량 */
    public static final int BASE_DAMAGE_PER_SECOND = 64;
    /** 글리치 피해량 계수 */
    public static final double GLITCH_DAMAGE_AMPLIFIER = 2.56;
    /** 초당 글리치 충전량 */
    public static final int GLITCH_GAIN_PER_SECOND = 8;
    /** 연사속도 */
    public static final FullAuto.FireRate FIRE_RATE = FullAuto.FireRate.RPM_1200;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 16;

    @Getter
    private static final DeltaWeaponInfo instance = new DeltaWeaponInfo();

    private DeltaWeaponInfo() {
        super(DeltaWeapon.class, Resource.DEFAULT, "광자 투사기",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("적을 공격하거나 아군을 치유할 수 있는 완드입니다.")
                        .addActionKeyInfo("연결: 워프 드라이브", ActionKey.LEFT_CLICK)
                        .addActionKeyInfo("광자 투사기", ActionKey.RIGHT_CLICK)
                        .build(),
                        new ActionInfoLore.NamedSection("연결: 워프 드라이브", ActionInfoLore.Section
                                .builder("바라보는 곳으로 순간이동한 뒤 일정 시간동안 유지되는 일방통행 포탈을 남깁니다. " +
                                        "포탈은 접촉한 대상을 순간이동 시킵니다. 이동 거리에 비례하여 <:GLITCH_DECREASE:글리치> 사용량이 증가합니다.")
                                .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, WarpDrive.MAX_DISTANCE)
                                .addValueInfo(TextIcon.GLITCH_DECREASE, Format.VARIABLE_WITH_DISTANCE,
                                        WarpDrive.GLITCH_USE_MIN, WarpDrive.GLITCH_USE_MAX, 0, WarpDrive.MAX_DISTANCE)
                                .addValueInfo(TextIcon.DURATION, Format.TIME, WarpDrive.DURATION.toSeconds())
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, WarpDrive.COOLDOWN.toSeconds())
                                .build()),
                        new ActionInfoLore.NamedSection("광자 투사기", ActionInfoLore.Section
                                .builder("바라보는 적에게 광선을 고정하여 지속적으로 <:DAMAGE:고정 피해>를 입히고 글리치를 충전합니다. " +
                                        "글리치에 비례하여 피해량이 증가합니다.")
                                .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_PER_SECOND,
                                        BASE_DAMAGE_PER_SECOND,
                                        BASE_DAMAGE_PER_SECOND + DeltaT1Info.MAX * GLITCH_DAMAGE_AMPLIFIER)
                                .addValueInfo(TextIcon.GLITCH, Format.PER_SECOND, GLITCH_GAIN_PER_SECOND)
                                .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME_WITH_RPM,
                                        60.0 / FIRE_RATE.getRoundsPerMinute(), FIRE_RATE.getRoundsPerMinute())
                                .build())));
    }


    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static final class Resource {
        /** 기본 */
        public static final short DEFAULT = 1561;
    }

    /**
     * 연결: 워프 드라이브의 정보.
     */
    @UtilityClass
    public static final class WarpDrive {
        /** 최대 거리 (단위: 블록) */
        public static final int MAX_DISTANCE = 16;
        /** 유지 시간 **/
        public static final Timespan DURATION = Timespan.ofSeconds(4);
        /** 공격 속도 **/
        public static final Timespan COOLDOWN = Timespan.ofSeconds(1);
        /** 글리치 최소 소모량 */
        public static final int GLITCH_USE_MIN = 16;
        /** 글리치 최대 소모량 */
        public static final int GLITCH_USE_MAX = 64;
    }
}
