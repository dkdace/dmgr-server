package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ChargeableSkill;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Set;

public final class NeaceA2 extends ChargeableSkill {
    /** 공격력 수정자 */
    private static final Modifier DAMAGE_MODIFIER = new Modifier(NeaceA2Info.DAMAGE_INCREMENT);
    /** 방어력 수정자 */
    private static final Modifier DEFENSE_MODIFIER = new Modifier(NeaceA2Info.DEFENSE_INCREMENT);

    public NeaceA2(@NonNull CombatUser combatUser, @NonNull NeaceA2Info skillInfo) {
        super(combatUser, skillInfo, NeaceA2Info.COOLDOWN, NeaceA2Info.MAX_DURATION.toSeconds());
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.SLOT_2);
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        ActionBarDisplay.Builder builder = ActionBarDisplay.builder(this).title()
                .durationBar(Timespan.ofSeconds(getStateValue()), Timespan.ofSeconds(maxStateValue));

        if (isDurationFinished())
            return builder.build();
        else
            return builder.keyInfo("해제").build();
    }

    @Override
    protected double getStateValueDecrement() {
        return 1;
    }

    @Override
    protected double getStateValueIncrement() {
        return getMaxStateValue() / NeaceA2Info.RECOVER_DURATION.toSeconds();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (!isDurationFinished()) {
            forceCancel();
            return;
        }

        setDuration();
        combatUser.getAbilityManager().getWeapon().setGlowing(true);

        NeaceA2Info.Effects.ON.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            if (getStateValue() <= 0)
                return false;

            NeaceA2Info.Effects.TICK.play(combatUser.getCenterLocation());
            if (i < 10)
                NeaceA2Info.Effects.playUseTick(i, combatUser.getLocation());

            return true;
        }, this::forceCancel, 1));
    }

    @Override
    public boolean isCancellable() {
        return combatUser.isDead() && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);

        combatUser.getAbilityManager().getWeapon().setGlowing(false);

        NeaceA2Info.Effects.OFF.play(combatUser.getLocation());
    }

    /**
     * 대상에게 축복 효과를 적용한다.
     *
     * @param target 적용 대상
     */
    void amplifyTarget(@NonNull Healable target) {
        target.getStatusEffectModule().apply(NeaceA2Buff.instance, Timespan.ofTicks(4));

        if (target instanceof CombatUser)
            ((CombatUser) target).addKillHelper(combatUser, this, NeaceA2Info.ASSIST_SCORE, Timespan.ofTicks(4));
    }

    /**
     * 축복 상태 효과 클래스.
     */
    private static final class NeaceA2Buff implements StatusEffect {
        private static final NeaceA2Buff instance = new NeaceA2Buff();

        @Override
        public boolean isPositive() {
            return true;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            combatEntity.getDamageModule().addModifier(DEFENSE_MODIFIER);
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackerModule().addModifier(DAMAGE_MODIFIER);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            combatEntity.getDamageModule().removeModifier(DEFENSE_MODIFIER);
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackerModule().removeModifier(DAMAGE_MODIFIER);
        }
    }
}
