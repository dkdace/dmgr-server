package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class InfernoA1Info extends ActiveSkillInfo {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 5 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.4 * 20);
    /** 수직 이동 강도 */
    public static final double PUSH_UP = 0.5;
    /** 수평 이동 강도 */
    public static final double PUSH_SIDE = 1.6;
    /** 피해량 */
    public static final int DAMAGE = 200;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 3.5;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.6;
    @Getter
    private static final InfernoA1Info instance = new InfernoA1Info();

    private InfernoA1Info() {
        super(1, "점프 부스터");
    }

    @Override
    @NonNull
    public InfernoA1 createSkill(@NonNull CombatUser combatUser) {
        return new InfernoA1(combatUser);
    }
}
