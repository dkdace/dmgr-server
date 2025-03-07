package com.dace.dmgr.combat.combatant.inferno.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Area;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;

@Setter
public final class InfernoP1 extends AbstractSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(InfernoP1Info.DEFENSE_INCREMENT);
    /** 활성화 가능 여부 */
    private boolean canActivate = false;

    public InfernoP1(@NonNull CombatUser combatUser) {
        super(combatUser);
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
        combatUser.getStatusEffectModule().apply(InfernoP1Buff.instance, combatUser, Timespan.ofTicks(InfernoP1Info.DURATION));
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 불꽃의 용기 상태 효과 클래스.
     */
    public static final class InfernoP1Buff extends StatusEffect {
        @Getter
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
