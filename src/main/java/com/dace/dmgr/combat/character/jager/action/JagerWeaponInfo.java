package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.action.weapon.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.gui.ItemBuilder;
import lombok.Getter;

public final class JagerWeaponInfo extends WeaponInfo {
    /** 피해량 */
    public static final int DAMAGE = 100;
    /** 사거리 */
    public static final int DISTANCE = 20;
    /** 투사체 속력 */
    public static final int VELOCITY = 80;
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

    public JagerWeaponInfo() {
        super("MK.73 ELNR", ItemBuilder.fromCSItem("MK73ELNR").build());
    }

    @Override
    public Weapon createWeapon(CombatUser combatUser) {
        return new JagerWeaponL(combatUser);
    }

    /**
     * 정조준 상태의 정보.
     */
    public static class SCOPE {
        /** 피해량 */
        static final int DAMAGE = 250;
        /** 피해량 감소 시작 거리 */
        static final int DAMAGE_DISTANCE = 30;
        /** 쿨타임 */
        static final long COOLDOWN = (long) (0.25 * 20);
        /** 장탄수 */
        static final int CAPACITY = 6;

        /**
         * 반동 정보.
         */
        public static class RECOIL {
            /** 수직 반동 */
            static final float UP = 2.8F;
            /** 수평 반동 */
            static final float SIDE = 0F;
            /** 수직 반동 분산도 */
            static final float UP_SPREAD = 0.3F;
            /** 수평 반동 분산도 */
            static final float SIDE_SPREAD = 0.4F;
        }
    }

    /**
     * 반동 정보.
     */
    public static class RECOIL {
        /** 수직 반동 */
        static final float UP = 0.8F;
        /** 수평 반동 */
        static final float SIDE = 0F;
        /** 수직 반동 분산도 */
        static final float UP_SPREAD = 0.1F;
        /** 수평 반동 분산도 */
        static final float SIDE_SPREAD = 0.05F;
    }
}
