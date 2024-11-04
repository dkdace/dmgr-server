package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

public final class QuakerT1Info extends TraitInfo {
    /** 상태 효과 저항 */
    public static final int STATUS_EFFECT_RESISTANCE = 35;
    @Getter
    private static final QuakerT1Info instance = new QuakerT1Info();

    private QuakerT1Info() {
        super("불굴",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("받는 모든 <:NEGATIVE_EFFECT:해로운 효과>의 시간이 감소합니다.")
                        .addValueInfo(TextIcon.NEGATIVE_EFFECT, Format.PERCENT, STATUS_EFFECT_RESISTANCE)
                        .build()
                )
        );
    }
}
