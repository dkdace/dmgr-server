package com.dace.dmgr.combat.combatant.jager.action;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.module.statuseffect.ValueStatusEffect;
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
     */
    static void addFreezeValue(@NonNull Damageable victim, int amount) {
        FreezeValue freezeValue = victim.getStatusEffectModule().apply(ValueStatusEffect.Type.FREEZE, victim, JagerT1Info.DURATION);
        freezeValue.setValue(freezeValue.getValue() + amount);
    }

    /**
     * 빙결 수치 상태 효과 클래스.
     */
    public static final class FreezeValue extends ValueStatusEffect {
        /** 수정자 */
        private final AbilityStatus.Modifier modifier = new AbilityStatus.Modifier(0);

        public FreezeValue() {
            super(StatusEffectType.SLOW, false, JagerT1Info.MAX);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(modifier);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            if (combatEntity instanceof Movable)
                modifier.setIncrement(-getValue());

            if (combatEntity.isCreature())
                JagerT1Info.PARTICLE.TICK_PARTICLE.play(combatEntity.getLocation().add(0, 0.5, 0), combatEntity.getWidth());
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            setValue(0);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(modifier);
        }

        @Override
        @NonNull
        public Set<@NonNull CombatRestriction> getCombatRestrictions(@NonNull Damageable combatEntity) {
            EnumSet<CombatRestriction> combatRestrictions = EnumSet.of(CombatRestriction.NONE);

            if (getValue() >= JagerT1Info.NO_SPRINT)
                combatRestrictions.add(CombatRestriction.SPRINT);
            if (getValue() >= JagerT1Info.NO_JUMP)
                combatRestrictions.add(CombatRestriction.JUMP);

            return combatRestrictions;
        }
    }
}
