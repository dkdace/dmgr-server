package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class NeaceP1Info extends PassiveSkillInfo<NeaceP1> {
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 100;
    /** 활성화 시간 (tick) */
    public static final long ACTIVATE_DURATION = (long) (2.5 * 20);
    @Getter
    private static final NeaceP1Info instance = new NeaceP1Info();

    private NeaceP1Info() {
        super(NeaceP1.class, "생명의 힘",
                "",
                "§f▍ 일정 시간동안 피해를 받지 않으면 §a" + TextIcon.HEAL + " 회복§f합니다.",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, ACTIVATE_DURATION / 20.0),
                MessageFormat.format("§a{0}§f {1}/초", TextIcon.HEAL, HEAL_PER_SECOND));
    }
}
