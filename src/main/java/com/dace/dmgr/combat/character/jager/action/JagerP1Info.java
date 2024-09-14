package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class JagerP1Info extends PassiveSkillInfo<JagerP1> {
    /** 지속시간 (tick) */
    public static final long DURATION = 3 * 20;
    @Getter
    private static final JagerP1Info instance = new JagerP1Info();

    private JagerP1Info() {
        super(JagerP1.class, "사냥의 미학",
                "",
                "§f▍ §d설랑§f이 공격한 적, §d곰덫§f에 걸린 적 및",
                "§f▍ §d빙결 수류탄§f으로 속박당한 적의 위치를 일정",
                "§f▍ 시간동안 탐지합니다.",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0));
    }
}
