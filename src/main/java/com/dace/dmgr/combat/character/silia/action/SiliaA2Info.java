package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class SiliaA2Info extends ActiveSkillInfo {
    /** 쿨타임 */
    public static final long COOLDOWN = 14 * 20;
    /** 시전 시간 */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 100;
    /** 사거리 */
    public static final int DISTANCE = 15;
    /** 투사체 속력 */
    public static final int VELOCITY = 25;
    /** 투사체 크기 */
    public static final double SIZE = 1.5;
    @Getter
    private static final SiliaA2Info instance = new SiliaA2Info();

    public SiliaA2Info() {
        super(2, "진권풍");
    }

    @Override
    public @NonNull SiliaA2 createSkill(@NonNull CombatUser combatUser) {
        return new SiliaA2(combatUser);
    }
}
