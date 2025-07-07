package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.TraitInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import lombok.Getter;

public final class SiliaT1Info extends TraitInfo<SiliaT1> {
    /** 치명타 배수 */
    public static final int CRIT_MULTIPLIER = 2;

    /** 치명타 점수 */
    public static final CombatScore CRIT_SCORE = new CombatScore("백어택", 3);

    @Getter
    private static final SiliaT1Info instance = new SiliaT1Info();

    private SiliaT1Info() {
        super(SiliaT1.class, "백어택",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("적의 뒤를 공격하면 <:DAMAGE_INCREASE:치명타>를 입힙니다.")
                        .addValueInfo(TextIcon.DAMAGE_INCREASE, "×" + CRIT_MULTIPLIER)
                        .build()));
    }
}
