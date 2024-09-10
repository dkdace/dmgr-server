package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class VellionP2Info extends PassiveSkillInfo<VellionP2> {
    /** 치유 피해량 비율 */
    public static final double HEAL_DAMAGE_RATIO = 0.2;
    @Getter
    private static final VellionP2Info instance = new VellionP2Info();

    private VellionP2Info() {
        super(VellionP2.class, "마력 흡수",
                "",
                "§f▍ 적에게 피해를 입히면 §a" + TextIcon.HEAL + " 회복§f합니다.",
                "",
                MessageFormat.format("§a{0}§f 피해량의 {1}%", TextIcon.HEAL, 100 * HEAL_DAMAGE_RATIO));
    }
}
