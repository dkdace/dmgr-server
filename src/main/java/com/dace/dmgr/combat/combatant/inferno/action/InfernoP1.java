package com.dace.dmgr.combat.combatant.inferno.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Area;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

@Setter
public final class InfernoP1 extends AbstractSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(InfernoP1Info.DEFENSE_INCREMENT);
    /** 활성화 가능 여부 */
    private boolean canActivate = false;

    public InfernoP1(@NonNull CombatUser combatUser) {
        super(combatUser, InfernoP1Info.getInstance(), Timespan.ZERO, Timespan.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        if (!combatUser.getStatusEffectModule().has(InfernoP1Buff.instance))
            return null;

        return ActionBarStringUtil.getDurationBar(this, combatUser.getStatusEffectModule().getDuration(InfernoP1Buff.instance), InfernoP1Info.DURATION);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && canActivate();
    }

    /**
     * 스킬 활성화 조건을 확인한다.
     *
     * @return 활성화 조건
     */
    private boolean canActivate() {
        canActivate = false;
        new InfernoP1Area().emit(combatUser.getLocation().add(0, 0.1, 0));

        return canActivate;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getStatusEffectModule().apply(InfernoP1Buff.instance, combatUser, InfernoP1Info.DURATION);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 불꽃의 용기 상태 효과 클래스.
     */
    private static final class InfernoP1Buff extends StatusEffect {
        private static final InfernoP1Buff instance = new InfernoP1Buff();

        private InfernoP1Buff() {
            super(true);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().addModifier(MODIFIER);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().removeModifier(MODIFIER);
        }
    }

    private final class InfernoP1Area extends Area<Damageable> {
        private InfernoP1Area() {
            super(combatUser, InfernoP1Info.DETECT_RADIUS, CombatUtil.EntityCondition.enemy(combatUser)
                    .and(combatEntity -> combatEntity.getStatusEffectModule().hasType(StatusEffectType.BURNING)));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            canActivate = true;
            return true;
        }
    }
}
