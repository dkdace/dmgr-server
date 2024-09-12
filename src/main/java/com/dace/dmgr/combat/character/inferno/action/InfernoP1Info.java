package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class InfernoP1Info extends PassiveSkillInfo<InfernoP1> {
    /** 방어력 증가량 */
    public static final int DEFENSE_INCREMENT = 40;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (1.5 * 20);
    @Getter
    private static final InfernoP1Info instance = new InfernoP1Info();

    private InfernoP1Info() {
        super(InfernoP1.class, "불꽃의 용기",
                "",
                "§f▍ 기본 무기로 적을 공격하는 동안 §6" + TextIcon.DEFENSE_INCREASE + " 방어력§f이",
                "§f▍ 증가합니다.",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§6{0}§f {1}%", TextIcon.DEFENSE_INCREASE, DEFENSE_INCREMENT));
    }
}
