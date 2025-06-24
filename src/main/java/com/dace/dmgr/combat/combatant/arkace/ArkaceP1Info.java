package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.PassiveSkillInfo;
import lombok.Getter;

public final class ArkaceP1Info extends PassiveSkillInfo<ArkaceP1> {
    /** 이동속도 증가량 */
    public static final int SPRINT_SPEED = 30;
    @Getter
    private static final ArkaceP1Info instance = new ArkaceP1Info();

    private ArkaceP1Info() {
        super(ArkaceP1.class, "강화된 신체",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("달리기 시 <:WALK_SPEED_INCREASE:이동 속도>가 빨라집니다.")
                        .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPRINT_SPEED)
                        .build()));
    }
}
