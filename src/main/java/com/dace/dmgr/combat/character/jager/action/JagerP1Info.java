package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class JagerP1Info extends PassiveSkillInfo {
    /** 이동속도 증가량 */
    public static final int SPEED = 15;
    /** 감지 범위 (단위: 블록) */
    public static final double DETECT_RADIUS = 10;
    @Getter
    private static final JagerP1Info instance = new JagerP1Info();

    private JagerP1Info() {
        super(1, "사냥의 미학");
    }

    @Override
    @NonNull
    public JagerP1 createSkill(@NonNull CombatUser combatUser) {
        return new JagerP1(combatUser);
    }
}
