package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.info.WeaponInfo;
import lombok.Getter;
import lombok.experimental.UtilityClass;

public final class MagrittaWeaponInfo extends WeaponInfo<MagrittaWeapon> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.5 * 20);
    /** 피해량 */
    public static final int DAMAGE = 40;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 16;
    /** 산탄 수 */
    public static final int PELLET_AMOUNT = 8;
    /** 탄퍼짐 */
    public static final double SPREAD = 18;
    /** 장탄수 */
    public static final int CAPACITY = 8;
    /** 재장전 시간 (tick) */
    public static final long RELOAD_DURATION = (long) (1.8 * 20);
    @Getter
    private static final MagrittaWeaponInfo instance = new MagrittaWeaponInfo();

    private MagrittaWeaponInfo() {
        super(MagrittaWeapon.class, RESOURCE.DEFAULT, "데스페라도");
    }

    /**
     * 반동 정보.
     */
    @UtilityClass
    public static class RECOIL {
        /** 수직 반동 */
        public static final double UP = 9.0;
        /** 수평 반동 */
        public static final double SIDE = 0;
        /** 수직 반동 분산도 */
        public static final double UP_SPREAD = 1.0;
        /** 수평 반동 분산도 */
        public static final double SIDE_SPREAD = 3.2;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 13;
    }
}
