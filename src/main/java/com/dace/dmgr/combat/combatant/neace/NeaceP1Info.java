package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.PassiveSkillInfo;
import lombok.Getter;

public final class NeaceP1Info extends PassiveSkillInfo<NeaceP1> {
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 100;
    /** 활성화 시간 */
    public static final Timespan ACTIVATE_DURATION = Timespan.ofSeconds(2.5);

    @Getter
    private static final NeaceP1Info instance = new NeaceP1Info();

    private NeaceP1Info() {
        super(NeaceP1.class, "생명의 힘",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("일정 시간동안 피해를 받지 않으면 <:HEAL:회복>합니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, ACTIVATE_DURATION.toSeconds())
                        .addValueInfo(TextIcon.HEAL, Format.PER_SECOND, HEAL_PER_SECOND)
                        .build()));
    }
}
