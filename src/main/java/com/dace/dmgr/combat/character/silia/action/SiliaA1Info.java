package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class SiliaA1Info extends ActiveSkillInfo {
    /** 쿨타임 */
    public static final long COOLDOWN = 8 * 20;
    /** 이동 거리 */
    public static final int MOVE_DISTANCE = 15;
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 피해 범위 */
    public static final double RADIUS = 2.5;
    @Getter
    private static final SiliaA1Info instance = new SiliaA1Info();

    public SiliaA1Info() {
        super(1, "연풍 가르기");
    }

    @Override
    public @NonNull SiliaA1 createSkill(@NonNull CombatUser combatUser) {
        return new SiliaA1(combatUser);
    }
}
