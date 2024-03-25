package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;

public final class SiliaWeaponInfo extends WeaponInfo {
    /** 피해량 */
    public static final int DAMAGE = 200;
    /** 사거리 */
    public static final double DISTANCE = 12;
    /** 투사체 속력 */
    public static final int VELOCITY = 35;
    /** 투사체 크기 */
    public static final double SIZE = 0.5;
    /** 쿨타임 */
    public static final long COOLDOWN = (long) (0.9 * 20);
    @Getter
    private static final SiliaWeaponInfo instance = new SiliaWeaponInfo();

    public SiliaWeaponInfo() {
        super(RESOURCE.DEFAULT, "접이식 마체테");
    }

    @Override
    public SiliaWeapon createWeapon(CombatUser combatUser) {
        return new SiliaWeapon(combatUser);
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    public interface RESOURCE {
        /** 기본 */
        short DEFAULT = 4;
        /** 사용 */
        short USE = 1561;
        /** 확장 */
        short EXTENDED = DEFAULT + 1000;
    }
}
