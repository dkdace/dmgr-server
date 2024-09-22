package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class QuakerT1Info extends TraitInfo {
    /** 상태 효과 저항 */
    public static final int STATUS_EFFECT_RESISTANCE = 35;
    @Getter
    private static final QuakerT1Info instance = new QuakerT1Info();

    private QuakerT1Info() {
        super("불굴",
                "",
                "§f▍ 받는 모든 §5" + TextIcon.NEGATIVE_EFFECT + " 해로운 효과§f의 시간이 감소합니다.",
                "",
                MessageFormat.format("§5{0}§f {1}%", TextIcon.NEGATIVE_EFFECT, STATUS_EFFECT_RESISTANCE));
    }
}
