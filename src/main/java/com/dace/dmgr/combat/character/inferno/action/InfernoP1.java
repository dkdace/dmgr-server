package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import lombok.*;

@Setter
public final class InfernoP1 extends AbstractSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "InfernoP1";

    InfernoP1(@NonNull CombatUser combatUser) {
        super(combatUser, InfernoP1Info.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    public long getDefaultCooldown() {
        return 0;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getStatusEffectModule().applyStatusEffect(combatUser, InfernoP1Buff.instance, InfernoP1Info.DURATION);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 불꽃의 용기 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class InfernoP1Buff implements StatusEffect {
        @Getter
        private static final InfernoP1Buff instance = new InfernoP1Buff();

        @Override
        @NonNull
        public StatusEffectType getStatusEffectType() {
            return StatusEffectType.NONE;
        }

        @Override
        public boolean isPositive() {
            return true;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Damageable)
                ((Damageable) combatEntity).getDamageModule().getDefenseMultiplierStatus().addModifier(MODIFIER_ID, InfernoP1Info.DEFENSE_INCREMENT);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Damageable)
                ((Damageable) combatEntity).getDamageModule().getDefenseMultiplierStatus().removeModifier(MODIFIER_ID);
        }
    }
}
