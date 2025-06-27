package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.TraitInfo;
import lombok.Getter;

public final class QuakerT1Info extends TraitInfo<QuakerT1> {
    /** 상태 효과 저항 */
    public static final int STATUS_EFFECT_RESISTANCE = 35;
    @Getter
    private static final QuakerT1Info instance = new QuakerT1Info();

    private QuakerT1Info() {
        super(QuakerT1.class, "불굴",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("받는 모든 <:NEGATIVE_EFFECT:해로운 효과>의 시간이 감소합니다.")
                        .addValueInfo(TextIcon.NEGATIVE_EFFECT, Format.PERCENT, STATUS_EFFECT_RESISTANCE)
                        .build()));
    }
}
