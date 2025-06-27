package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.combat.ability.Trait;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import lombok.NonNull;

public final class QuakerT1 extends Trait {
    /** 특성 수정자 */
    private static final Modifier TRAIT_MODIFIER = new Modifier(QuakerT1Info.STATUS_EFFECT_RESISTANCE);

    public QuakerT1(@NonNull CombatUser combatUser, @NonNull QuakerT1Info traitInfo) {
        super(combatUser, traitInfo);
    }

    /**
     * 전투원을 선택했을 때 실행할 작업.
     */
    void onSet() {
        combatUser.getStatusEffectModule().addModifier(TRAIT_MODIFIER);
    }
}
