package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.info.WeaponInfo;
import lombok.Getter;

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
        super(InfernoWeapon.class, RESOURCE.DEFAULT, "파이어스톰");
    }

    /**
     * 화염탄의 정보.
     */
    public interface FIREBALL {
        /** 쿨타임 (tick) */
        long COOLDOWN = 1 * 20;
        /** 피해량 (폭발) */
        int DAMAGE_EXPLODE = 100;
        /** 피해량 (직격) */
        int DAMAGE_DIRECT = 40;
        /** 사거리 (단위: 블록) */
        int DISTANCE = 10;
        /** 투사체 속력 (단위: 블록/s) */
        int VELOCITY = 30;
        /** 투사체 크기 (단위: 블록) */
        double SIZE = 0.5;
        /** 피해 범위 (단위: 블록) */
        double RADIUS = 2.5;
        /** 탄환 소모량 */
        int CAPACITY_CONSUME = 40;
        /** 넉백 강도 */
        double KNOCKBACK = 0.3;

        /**
         * 반동 정보.
         */
        interface RECOIL {
            /** 수직 반동 */
            double UP = 5.0;
            /** 수평 반동 */
            double SIDE = 0;
            /** 수직 반동 분산도 */
            double UP_SPREAD = 1.0;
            /** 수평 반동 분산도 */
            double SIDE_SPREAD = 0.8;
        }
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    public interface RESOURCE {
        /** 기본 */
        short DEFAULT = 12;
    }
}
