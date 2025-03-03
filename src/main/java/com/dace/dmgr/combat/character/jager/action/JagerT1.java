package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class JagerT1 {
    /**
     * 피격자의 빙결 수치를 증가시킨다.
     *
     * @param victim 피격자
     * @param amount 증가량
     */
    static void addFreezeValue(@NonNull Damageable victim, int amount) {
        victim.getPropertyManager().addValue(Property.FREEZE, amount);
        victim.getStatusEffectModule().applyStatusEffect(victim, FreezeValue.instance, JagerT1Info.DURATION);
    }

    /**
     * 빙결 수치 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class FreezeValue implements StatusEffect {
        private static final FreezeValue instance = new FreezeValue();
        /** 수정자 ID */
        private static final String MODIFIER_ID = "JagerT1";

        @Override
        @NonNull
        public StatusEffectType getStatusEffectType() {
            return StatusEffectType.NONE;
        }

        @Override
        public boolean isPositive() {
            return false;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            // 미사용
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            if (combatEntity.getDamageModule().isLiving())
                JagerT1Info.PARTICLE.TICK_PARTICLE.play(combatEntity.getEntity().getLocation().add(0, 0.5, 0), combatEntity.getWidth());

            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID,
                        -combatEntity.getPropertyManager().getValue(Property.FREEZE));
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getPropertyManager().setValue(Property.FREEZE, 0);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        }

        @Override
        public long getCombatRestrictions(@NonNull Damageable combatEntity) {
            int freezeValue = combatEntity.getPropertyManager().getValue(Property.FREEZE);
            long restrictions = CombatRestrictions.NONE;

            if (freezeValue >= JagerT1Info.NO_SPRINT)
                restrictions |= CombatRestrictions.SPRINT;
            if (freezeValue >= JagerT1Info.NO_JUMP)
                restrictions |= CombatRestrictions.JUMP;

            return restrictions;
        }
    }
}
