package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;

public final class QuakerWeaponInfo extends WeaponInfo {
    /** 피해량 */
    public static final int DAMAGE = 320;
    /** 사거리 */
    public static final double DISTANCE = 3.5;
    /** 쿨타임 */
    public static final long COOLDOWN = (long) (1.1 * 20);
    @Getter
    private static final QuakerWeaponInfo instance = new QuakerWeaponInfo();

    public QuakerWeaponInfo() {
        super(RESOURCE.DEFAULT, "타바르진");
    }

    @Override
    public QuakerWeapon createWeapon(CombatUser combatUser) {
        return new QuakerWeapon(combatUser);
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    public interface RESOURCE {
        /** 기본 */
        short DEFAULT = 3;
        /** 사용 */
        short USE = 1561;
    }
}
