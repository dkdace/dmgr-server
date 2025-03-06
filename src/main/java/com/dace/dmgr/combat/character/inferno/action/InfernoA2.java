package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.module.statuseffect.Grounding;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.WeakHashMap;

public final class InfernoA2 extends ActiveSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-InfernoA2Info.HEAL_DECREMENT);
    /** 처치 지원 점수 제한시간 타임스탬프 목록 (피격자 : 종료 시점) */
    private final WeakHashMap<CombatUser, Timestamp> assistScoreTimeLimitTimestampMap = new WeakHashMap<>();

    public InfernoA2(@NonNull CombatUser combatUser) {
        super(combatUser, InfernoA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public long getDefaultCooldown() {
        return InfernoA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return InfernoA2Info.DURATION;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        InfernoA2Info.SOUND.USE.play(combatUser.getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (i % 4 == 0)
                new InfernoA2Area().emit(combatUser.getEntity().getEyeLocation());

            InfernoA2Info.SOUND.TICK.play(combatUser.getLocation());
            InfernoA2Info.PARTICLE.TICK_CORE.play(combatUser.getLocation().add(0, 1, 0));
            playTickEffect(i);
        }, 1, InfernoA2Info.DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
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
     * 플레이어에게 처치 지원 점수를 지급한다.
     *
     * @param victim 피격자
     */
    public void applyAssistScore(@NonNull CombatUser victim) {
        Timestamp expiration = assistScoreTimeLimitTimestampMap.get(victim);
        if (expiration != null && expiration.isAfter(Timestamp.now()))
            combatUser.addScore("처치 지원", InfernoA2Info.ASSIST_SCORE);
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
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null,
                    false, true)) {
                target.getStatusEffectModule().apply(InfernoA2Burning.instance, combatUser, Timespan.ofTicks(10));
                target.getStatusEffectModule().apply(Grounding.getInstance(), combatUser, Timespan.ofTicks(10));

                if (target instanceof CombatUser) {
                    combatUser.addScore("적 고정", (double) (InfernoA2Info.EFFECT_SCORE_PER_SECOND * 4) / 20);
                    assistScoreTimeLimitTimestampMap.put((CombatUser) target, Timestamp.now().plus(Timespan.ofTicks(10)));
                }
            }

            return true;
        }
    }
}
