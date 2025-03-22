package com.dace.dmgr.combat.combatant.inferno.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.module.statuseffect.Grounding;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Getter
public final class InfernoA2 extends ActiveSkill implements HasBonusScore {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-InfernoA2Info.HEAL_DECREMENT);
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public InfernoA2(@NonNull CombatUser combatUser) {
        super(combatUser, InfernoA2Info.getInstance(), InfernoA2Info.COOLDOWN, InfernoA2Info.DURATION, 1);
        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", InfernoA2Info.ASSIST_SCORE);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return isDurationFinished() ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        InfernoA2Info.SOUND.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            if (i % 4 == 0)
                new InfernoA2Area().emit(combatUser.getEntity().getEyeLocation());

            InfernoA2Info.SOUND.TICK.play(combatUser.getLocation());
            playTickEffect(i);
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
    public boolean isAssistMode() {
        return true;
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playTickEffect(long i) {
        Location loc = combatUser.getLocation().add(0, 1, 0);
        loc.setYaw(0);
        loc.setPitch(0);

        InfernoA2Info.PARTICLE.TICK_CORE.play(loc);

        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 3; j++) {
            long index = i * 3 + j;
            long yaw = index * 5;
            long pitch = index * 7;

            for (int k = 0; k < 3; k++) {
                yaw += 120;
                pitch -= 120;
                Vector vec = VectorUtil.getRotatedVector(axis, VectorUtil.getRotatedVector(vector, axis, yaw), pitch);
                Location loc2 = loc.clone().add(vec.clone().multiply(1.8));

                InfernoA2Info.PARTICLE.TICK_DECO.play(loc2, vec);
            }
        }
    }

    /**
     * 화염 상태 효과 클래스.
     */
    private static final class InfernoA2Burning extends Burning {
        private static final InfernoA2Burning instance = new InfernoA2Burning();

        private InfernoA2Burning() {
            super(InfernoA2Info.FIRE_DAMAGE_PER_SECOND, true);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Healable)
                ((Healable) combatEntity).getDamageModule().getHealMultiplierStatus().addModifier(MODIFIER);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Healable)
                ((Healable) combatEntity).getDamageModule().getHealMultiplierStatus().removeModifier(MODIFIER);
        }
    }

    private final class InfernoA2Area extends Area<Damageable> {
        private InfernoA2Area() {
            super(combatUser, InfernoA2Info.RADIUS, CombatUtil.EntityCondition.enemy(combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null, false, true)) {
                target.getStatusEffectModule().apply(InfernoA2Burning.instance, combatUser, Timespan.ofTicks(10));
                target.getStatusEffectModule().apply(Grounding.getInstance(), combatUser, Timespan.ofTicks(10));

                if (target instanceof CombatUser) {
                    combatUser.addScore("적 고정", InfernoA2Info.EFFECT_SCORE_PER_SECOND * 4 / 20.0);
                    bonusScoreModule.addTarget((CombatUser) target, Timespan.ofTicks(10));
                }
            }

            return true;
        }
    }
}
