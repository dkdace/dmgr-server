package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class ArkaceP1Info extends PassiveSkillInfo<ArkaceP1> {
    /** 이동속도 증가량 */
    public static final int SPRINT_SPEED = 30;
    @Getter
    private static final ArkaceP1Info instance = new ArkaceP1Info();

    private ArkaceP1Info() {
        super(ArkaceP1.class, "강화된 신체",
                "",
                "§f▍ 달리기 시 §b" + TextIcon.WALK_SPEED_INCREASE + " 이동 속도§f가 빨라집니다.",
                "",
                MessageFormat.format("§b{0}§f {1}%", TextIcon.WALK_SPEED_INCREASE, SPRINT_SPEED));
    }
}
