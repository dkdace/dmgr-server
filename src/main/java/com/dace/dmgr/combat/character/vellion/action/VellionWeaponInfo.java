package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class VellionWeaponInfo extends WeaponInfo {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.5 * 20);
    /** 피해량 */
    public static final int DAMAGE = 120;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 30;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 35;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.3;
    @Getter
    private static final VellionWeaponInfo instance = new VellionWeaponInfo();

    private VellionWeaponInfo() {
        super(RESOURCE.DEFAULT, "절멸");
    }

    @Override
    @NonNull
    public VellionWeapon createWeapon(@NonNull CombatUser combatUser) {
        return new VellionWeapon(combatUser);
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    public interface RESOURCE {
        /** 기본 */
        short DEFAULT = 14;
    }
}
