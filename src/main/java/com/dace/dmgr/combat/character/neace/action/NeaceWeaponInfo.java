package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.info.WeaponInfo;
import lombok.Getter;
import lombok.experimental.UtilityClass;

public final class NeaceWeaponInfo extends WeaponInfo<NeaceWeapon> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.4 * 20);
    /** 피해량 */
    public static final int DAMAGE = 100;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 20;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 40;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.16;
    @Getter
    private static final NeaceWeaponInfo instance = new NeaceWeaponInfo();

    private NeaceWeaponInfo() {
        super(NeaceWeapon.class, RESOURCE.DEFAULT, "이중성");
    }

    /**
     * 치유 광선의 정보.
     */
    @UtilityClass
    public static class HEAL {
        /** 초당 치유량 */
        public static final int HEAL_PER_SECOND = 250;
        /** 최대 거리 (단위: 블록) */
        public static final int MAX_DISTANCE = 15;
        /** 대상 위치 통과 불가 시 초기화 제한 시간 (tick) */
        public static final long BLOCK_RESET_DELAY = 2 * 20;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 5;
    }
}
