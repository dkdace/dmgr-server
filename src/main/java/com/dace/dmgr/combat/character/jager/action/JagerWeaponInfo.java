package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class JagerWeaponInfo extends WeaponInfo {
    /** 피해량 */
    public static final int DAMAGE = 100;
    /** 사거리 */
    public static final int DISTANCE = 20;
    /** 투사체 속력 */
    public static final int VELOCITY = 80;
    /** 탄퍼짐 */
    public static final double SPREAD = 5;
    /** 빙결량 */
    public static final int FREEZE = 15;
    /** 쿨타임 */
    public static final long COOLDOWN = (long) (0.25 * 20);
    /** 장탄수 */
    public static final int CAPACITY = 8;
    /** 재장전 시간 */
    public static final long RELOAD_DURATION = 2 * 20;
    /** 무기 교체 시간 */
    public static final long SWAP_DURATION = (long) (0.25 * 20);
    /** 조준 시 이동속도 감소량 */
    public static final int AIM_SPEED = 30;
    @Getter
    private static final JagerWeaponInfo instance = new JagerWeaponInfo();

    private JagerWeaponInfo() {
        super(RESOURCE.DEFAULT, "MK.73 ELNR");
    }

    @Override
    @NonNull
    public JagerWeaponL createWeapon(@NonNull CombatUser combatUser) {
        return new JagerWeaponL(combatUser);
    }

    /**
     * 정조준 상태의 정보.
     */
    public interface SCOPE {
        /** 피해량 */
        int DAMAGE = 250;
        /** 피해량 감소 시작 거리 */
        int DAMAGE_DISTANCE = 30;
        /** 쿨타임 */
        long COOLDOWN = (long) (0.25 * 20);
        /** 장탄수 */
        int CAPACITY = 6;
        /** 확대 레벨 */
        Aimable.ZoomLevel ZOOM_LEVEL = Aimable.ZoomLevel.L4;

        /**
         * 반동 정보.
         */
        interface RECOIL {
            /** 수직 반동 */
            double UP = 2.8;
            /** 수평 반동 */
            double SIDE = 0;
            /** 수직 반동 분산도 */
            double UP_SPREAD = 0.3;
            /** 수평 반동 분산도 */
            double SIDE_SPREAD = 0.4;
        }
    }

    /**
     * 반동 정보.
     */
    public interface RECOIL {
        /** 수직 반동 */
        double UP = 0.8;
        /** 수평 반동 */
        double SIDE = 0;
        /** 수직 반동 분산도 */
        double UP_SPREAD = 0.1;
        /** 수평 반동 분산도 */
        double SIDE_SPREAD = 0.05;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    public interface RESOURCE {
        /** 기본 */
        short DEFAULT = 2;
    }
}
