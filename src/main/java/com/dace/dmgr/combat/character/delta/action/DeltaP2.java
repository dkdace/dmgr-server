package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.util.GlowUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;

public class DeltaP2 extends AbstractSkill {

    public DeltaP2(CombatUser combatUser) {
        super(combatUser, DeltaP2Info.getInstance());
    }

    @Override
    public @NonNull ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[] {ActionKey.PERIODIC_2};
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
        setDuration();

        CombatEntity[] targets = CombatUtil.getNearCombatEntities(
                combatUser.getGame(),
                combatUser.getCenterLocation(),
                DeltaP2Info.DETECT_RADIUS,
                combatEntity -> combatUser.isEnemy(combatEntity)
                        && combatEntity instanceof Damageable
                        && combatUser.isInSight((Damageable) combatEntity)
        );

        for (CombatEntity target: targets) {
            if (!(target instanceof Damageable)) continue;
            Damageable damageable = (Damageable) target;

            damageable.getPropertyManager().addValue(Property.NEURAL_LINK, DeltaP2Info.UPDATE_TICK);
            damageable.getStatusEffectModule().applyStatusEffect(combatUser, NeuralLinkValue.instance, 5);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class NeuralLinkValue implements StatusEffect {
        private static final NeuralLinkValue instance = new NeuralLinkValue();

        @Override
        public @NonNull StatusEffectType getStatusEffectType() {
            return StatusEffectType.NONE;
        }

        @Override
        public boolean isPositive() {
            return true;    // 일단 non-negative...
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            // 미사용
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            if (combatEntity.getPropertyManager().getValue(Property.NEURAL_LINK) >= DeltaP2Info.GAZING_DURATION) {
                GlowUtil.setGlowing(combatEntity.getEntity(), ChatColor.RED, provider.getEntity(), DeltaP2Info.UPDATE_TICK);
            }
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getPropertyManager().setValue(Property.NEURAL_LINK, 0);
        }
    }
}

