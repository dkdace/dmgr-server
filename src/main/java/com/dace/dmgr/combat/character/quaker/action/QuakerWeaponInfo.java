package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.info.WeaponInfo;
import lombok.Getter;
import lombok.experimental.UtilityClass;

public final class QuakerWeaponInfo extends WeaponInfo<QuakerWeapon> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (1.1 * 20);
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.4 * 20);
    /** 피해량 */
    public static final int DAMAGE = 320;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 3.5;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 0.5;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.3;
    @Getter
    private static final QuakerWeaponInfo instance = new QuakerWeaponInfo();

    private QuakerWeaponInfo() {
        super(QuakerWeapon.class, RESOURCE.DEFAULT, "타바르진");
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 3;
    }
}
