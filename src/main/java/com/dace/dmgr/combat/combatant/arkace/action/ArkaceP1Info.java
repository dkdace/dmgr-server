package com.dace.dmgr.combat.combatant.arkace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class ArkaceP1Info extends PassiveSkillInfo<ArkaceP1> {
    /** 이동속도 증가량 */
    public static final int SPRINT_SPEED = 30;
    @Getter
    private static final ArkaceP1Info instance = new ArkaceP1Info();

    private ArkaceP1Info() {
        super(ArkaceP1.class, "강화된 신체",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("달리기 시 <:WALK_SPEED_INCREASE:이동 속도>가 빨라집니다.")
                        .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPRINT_SPEED)
                        .build()));
    }
}
