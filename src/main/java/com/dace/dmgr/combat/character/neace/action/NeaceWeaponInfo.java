package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class NeaceWeaponInfo extends WeaponInfo {
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
        super(RESOURCE.DEFAULT, "이중성");
    }

    @Override
    @NonNull
    public NeaceWeapon createWeapon(@NonNull CombatUser combatUser) {
        return new NeaceWeapon(combatUser);
    }

    /**
     * 치유 광선의 정보.
     */
    public interface HEAL {
        /** 초당 치유량 */
        int HEAL_PER_SECOND = 250;
        /** 최대 거리 (단위: 블록) */
        int MAX_DISTANCE = 15;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    public interface RESOURCE {
        /** 기본 */
        short DEFAULT = 5;
    }
}
