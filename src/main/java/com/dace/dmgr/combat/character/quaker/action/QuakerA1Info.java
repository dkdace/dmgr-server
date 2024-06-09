package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class QuakerA1Info extends ActiveSkillInfo {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 1 * 20;
    /** 사망 시 쿨타임 (tick) */
    public static final long COOLDOWN_DEATH = 4 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.4 * 20);
    /** 체력 */
    public static final int HEALTH = 5000;
    /** 체력 최대 회복 시간 (tick) */
    public static final int RECOVER_DURATION = 12 * 20;
    /** 사용 중 이동속도 감소량 */
    public static final int USE_SLOW = 25;

    /** 방어 점수 */
    public static final int BLOCK_SCORE = 75;
    /** 파괴 점수 */
    public static final int DEATH_SCORE = 20;
    @Getter
    private static final QuakerA1Info instance = new QuakerA1Info();

    private QuakerA1Info() {
        super(1, "불굴의 방패");
    }

    @Override
    @NonNull
    public QuakerA1 createSkill(@NonNull CombatUser combatUser) {
        return new QuakerA1(combatUser);
    }
}
