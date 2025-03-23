package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class JagerP1Info extends PassiveSkillInfo<JagerP1> {
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(3);
    @Getter
    private static final JagerP1Info instance = new JagerP1Info();

    private JagerP1Info() {
        super(JagerP1.class, "사냥의 미학",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("<d::설랑>이 공격한 적, <d::곰덫>에 걸린 적 및 <d::빙결 수류탄>으로 속박당한 적의 위치를 일정 시간동안 탐지합니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .build()));
    }
}
