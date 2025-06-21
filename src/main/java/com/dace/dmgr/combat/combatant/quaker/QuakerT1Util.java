package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class QuakerT1Util {
    /** 특성 수정자 */
    private static final Modifier TRAIT_MODIFIER = new Modifier(QuakerT1Info.STATUS_EFFECT_RESISTANCE);

    /**
     * 전투원을 선택했을 때 실행할 작업.
     *
     * @param combatUser 대상 플레이어
     */
    static void onSet(@NonNull CombatUser combatUser) {
        combatUser.getStatusEffectModule().addModifier(TRAIT_MODIFIER);
    }
}
