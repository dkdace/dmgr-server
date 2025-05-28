package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.EnumSet;
import java.util.Set;

@UtilityClass
public final class JagerT1 {
    /**
     * 피격자의 빙결 수치를 증가시킨다.
     *
     * @param victim 피격자
     * @param amount 증가량
     * @return 빙결 수치 상태 효과
     */
    @NonNull
    static FreezeValue addFreezeValue(@NonNull Damageable victim, int amount) {
        FreezeValue freezeValue = victim.getStatusEffectModule().get(FreezeValue.class);
        if (freezeValue == null)
            freezeValue = new FreezeValue();

        victim.getStatusEffectModule().apply(freezeValue, JagerT1Info.DURATION);
        freezeValue.addValue(amount);

        return freezeValue;
    }

    /**
     * 빙결 수치 상태 효과 클래스.
     */
    public static final class FreezeValue extends Slow {
        private FreezeValue() {
            super(0);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            if (combatEntity.isCreature())
                JagerT1Info.Particles.TICK_PARTICLE.play(combatEntity.getLocation().add(0, 0.5, 0), combatEntity.getWidth());
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
