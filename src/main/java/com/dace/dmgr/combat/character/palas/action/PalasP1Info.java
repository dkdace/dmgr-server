package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class PalasP1Info extends PassiveSkillInfo<PalasP1> {
    /** 치유량 배수 */
    public static final int HEAL_MULTIPLIER = 2;

    @Getter
    private static final PalasP1Info instance = new PalasP1Info();

    private PalasP1Info() {
        super(PalasP1.class, "응급 치료",
                "",
                "§f▍ 치명상인 아군을 치유하면 §a" + TextIcon.HEAL_INCREASE + " 치유량§f이 증가합니다.",
                "",
                MessageFormat.format("§a{0}§f ×{1}", TextIcon.HEAL_INCREASE, HEAL_MULTIPLIER));
    }
}
