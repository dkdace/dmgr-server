package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.info.WeaponInfo;
import lombok.Getter;

public final class SiliaWeaponInfo extends WeaponInfo<SiliaWeapon> {
    /** 피해량 */
    public static final int DAMAGE = 200;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 12;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 35;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.5;
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.9 * 20);
    @Getter
    private static final SiliaWeaponInfo instance = new SiliaWeaponInfo();

    private SiliaWeaponInfo() {
        super(SiliaWeapon.class, RESOURCE.DEFAULT, "접이식 마체테");
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    public interface RESOURCE {
        /** 기본 */
        short DEFAULT = 4;
        /** 확장 */
        short EXTENDED = DEFAULT + 1000;
    }
}
