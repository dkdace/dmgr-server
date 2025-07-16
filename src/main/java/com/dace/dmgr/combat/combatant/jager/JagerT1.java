package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.combat.ability.Trait;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Set;

public final class JagerT1 extends Trait {
    public JagerT1(@NonNull CombatUser combatUser, @NonNull JagerT1Info traitInfo) {
        super(combatUser, traitInfo);
    }

    /**
     * 피격자의 빙결 수치를 증가시킨다.
     *
     * @param victim 피격자
     * @param amount 증가량
     * @return 빙결 수치 상태 효과
     */
    @NonNull
    static ValueEffect addValue(@NonNull Damageable victim, int amount) {
        ValueEffect valueEffect = victim.getStatusEffectModule().get(ValueEffect.class);
        if (valueEffect == null)
            valueEffect = new ValueEffect();

        victim.getStatusEffectModule().apply(valueEffect, JagerT1Info.DURATION);
        valueEffect.addValue(amount);

        return valueEffect;
    }

    /**
     * 빙결 수치 상태 효과 클래스.
     */
    static final class ValueEffect extends Slow {
        private ValueEffect() {
            super(0);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            if (combatEntity.isCreature())
                JagerT1Info.Effects.TICK.apply(combatEntity).play(combatEntity.getLocation().add(0, 0.5, 0));
        }

        /**
         * 현재 빙결 수치를 반환한다.
         *
         * @return 빙결 수치
         */
        int getValue() {
            return (int) getDecrement();
        }

        /**
         * 빙결 수치를 증가시킨다.
         *
         * @param amount 추가할 빙결 수치
         */
        private void addValue(int amount) {
            setDecrement(Math.min(JagerT1Info.MAX, Math.max(0, getDecrement() + amount)));
        }

        @Override
        @NonNull
        public Set<@NonNull CombatRestriction> getCombatRestrictions(@NonNull Damageable combatEntity) {
            EnumSet<CombatRestriction> combatRestrictions = EnumSet.of(CombatRestriction.NONE);

            if (getDecrement() >= JagerT1Info.NO_SPRINT)
                combatRestrictions.add(CombatRestriction.SPRINT);
            if (getDecrement() >= JagerT1Info.NO_JUMP)
                combatRestrictions.add(CombatRestriction.JUMP);

            return combatRestrictions;
        }
    }
}
