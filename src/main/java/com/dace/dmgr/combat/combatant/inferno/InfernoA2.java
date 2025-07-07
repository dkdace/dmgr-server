package com.dace.dmgr.combat.combatant.inferno;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.skill.HasBonusScore;
import com.dace.dmgr.combat.ability.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.module.statuseffect.Grounding;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public final class InfernoA2 extends ActiveSkill implements HasBonusScore {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-InfernoA2Info.HEAL_DECREMENT);
    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 화염 상태 효과 */
    private final InfernoA2Burning burning;

    public InfernoA2(@NonNull CombatUser combatUser, @NonNull InfernoA2Info skillInfo) {
        super(combatUser, skillInfo, InfernoA2Info.COOLDOWN, InfernoA2Info.DURATION);

        this.bonusScoreModule = new BonusScoreModule(this);
        this.burning = new InfernoA2Burning();
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished())
            return null;

        return ActionBarDisplay.builder(this).title().durationBar().build();
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_2;
    }

    @Override
    public void onSlot() {
        setDuration();

        InfernoA2Info.Effects.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            if (i % 4 == 0)
                new InfernoA2Area().emit(combatUser.getEntity().getEyeLocation());

            InfernoA2Info.Effects.playTick(i, combatUser.getLocation());
        }, 1, InfernoA2Info.DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    @Override
    @NonNull
    public CombatScore getCombatScore() {
        return InfernoA2Info.ASSIST_SCORE;
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    /**
     * 화염 상태 효과 클래스.
     */
    private final class InfernoA2Burning extends Burning {
        private InfernoA2Burning() {
            super(combatUser, InfernoA2Info.FIRE_DAMAGE_PER_SECOND, true);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            if (combatEntity instanceof Healable)
                ((Healable) combatEntity).getHealModule().addModifier(MODIFIER);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            if (combatEntity instanceof Healable)
                ((Healable) combatEntity).getHealModule().removeModifier(MODIFIER);
        }
    }

    private final class InfernoA2Area extends Area<Damageable> {
        private InfernoA2Area() {
            super(combatUser, InfernoA2Info.RADIUS, EntityCondition.enemy(combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null, false, true)) {
                target.getStatusEffectModule().apply(burning, Timespan.ofTicks(10));
                target.getStatusEffectModule().apply(Grounding.getInstance(), Timespan.ofTicks(10));

                if (target.isGoalTarget()) {
                    combatUser.addScore(InfernoA2Info.EFFECT_SCORE_PER_SECOND.multiplyScore(4.0 / 20));
                    bonusScoreModule.addTarget(target, Timespan.ofTicks(10));
                }
            }

            return true;
        }
    }
}
