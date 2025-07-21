package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.skill.Targeted;
import com.dace.dmgr.combat.ability.skill.module.TargetModule;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import lombok.NonNull;
import org.bukkit.inventory.MainHand;

public final class NeaceA1 extends ActiveSkill implements Targeted<Healable> {
    /** 타겟 모듈 */
    private final TargetModule<Healable> targetModule;

    public NeaceA1(@NonNull CombatUser combatUser, @NonNull NeaceA1Info skillInfo) {
        super(combatUser, skillInfo, NeaceA1Info.COOLDOWN, Timespan.MAX);
        this.targetModule = new TargetModule<>(this);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && targetModule.findTarget();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_1;
    }

    @Override
    public void onSlot() {
        setCooldown();

        Healable target = targetModule.getCurrentTarget();

        ValueEffect valueEffect = target.getStatusEffectModule().get(ValueEffect.class);
        if (valueEffect == null)
            valueEffect = new ValueEffect();

        target.getStatusEffectModule().apply(valueEffect, NeaceA1Info.DURATION);

        NeaceA1Info.Effects.playUse(combatUser.getLocation(), combatUser.getArmLocation(MainHand.RIGHT), target.getCenterLocation());
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public int getTargetMaxDistance() {
        return NeaceA1Info.MAX_DISTANCE;
    }

    @Override
    @NonNull
    public EntityCondition<Healable> getTargetEntityCondition() {
        return EntityCondition.team(combatUser).exclude(combatUser)
                .and(combatEntity -> !combatEntity.getStatusEffectModule().has(ValueEffect.class));
    }

    /**
     * 구원의 표식 상태 효과 클래스.
     */
    private final class ValueEffect implements StatusEffect {
        private double heal = 0;

        @Override
        public boolean isPositive() {
            return true;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            // 미사용
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            NeaceA1Info.Effects.MARK.play(combatEntity.getLocation().add(0, combatEntity.getHeight() + 0.5, 0));

            if (combatUser.isRemoved()) {
                combatEntity.getStatusEffectModule().remove(this);
                return;
            }

            if (!(combatEntity instanceof Healable) || combatEntity.getDamageModule().isFullHealth()
                    || ((NeaceWeapon) combatUser.getAbilityManager().getWeapon()).isHealing((Healable) combatEntity))
                return;

            double amount = NeaceA1Info.HEAL_PER_SECOND / 20.0;
            if (((Healable) combatEntity).getHealModule().heal(combatUser, amount, true))
                heal += amount;

            if (heal >= NeaceA1Info.MAX_HEAL)
                combatEntity.getStatusEffectModule().remove(this);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            // 미사용
        }
    }
}
