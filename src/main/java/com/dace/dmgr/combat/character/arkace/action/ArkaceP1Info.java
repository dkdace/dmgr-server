package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class ArkaceP1Info extends PassiveSkillInfo {
    /** 이동속도 증가량 */
    public static final int SPRINT_SPEED = 30;
    @Getter
    private static final ArkaceP1Info instance = new ArkaceP1Info();

    private ArkaceP1Info() {
        super(1, "강화된 신체",
                "",
                "§f달리기의 §b" + TextIcon.WALK_SPEED_INCREASE + " 속도§f가 빨라집니다.",
                "",
                "§b" + TextIcon.WALK_SPEED_INCREASE + "§f 30%");
    }

    @Override
    @NonNull
    public ArkaceP1 createSkill(@NonNull CombatUser combatUser) {
        return new ArkaceP1(combatUser);
    }
}
