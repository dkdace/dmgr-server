package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class QuakerWeaponInfo extends WeaponInfo {
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
        super(RESOURCE.DEFAULT, "타바르진");
    }

    @Override
    @NonNull
    public QuakerWeapon createWeapon(@NonNull CombatUser combatUser) {
        return new QuakerWeapon(combatUser);
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    public interface RESOURCE {
        /** 기본 */
        short DEFAULT = 3;
    }
}
