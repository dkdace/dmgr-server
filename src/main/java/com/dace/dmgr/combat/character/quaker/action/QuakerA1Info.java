package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;

public final class QuakerA1Info extends ActiveSkillInfo {
    /** 사망 시 쿨타임 */
    public static final long COOLDOWN_DEATH = 4 * 20;
    /** 체력 */
    public static final int HEALTH = 5000;
    /** 체력 최대 회복 시간 */
    public static final int RECOVER_DURATION = 12;
    /** 사용 중 이동속도 감소량 */
    public static final int USE_SPEED = 25;
    @Getter
    private static final QuakerA1Info instance = new QuakerA1Info();

    public QuakerA1Info() {
        super(1, "불굴의 방패");
    }

    @Override
    public QuakerA1 createSkill(CombatUser combatUser) {
        return new QuakerA1(combatUser);
    }
}
