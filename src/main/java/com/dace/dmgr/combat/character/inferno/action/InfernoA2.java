package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.module.statuseffect.Grounding;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class InfernoA2 extends ActiveSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    public static final String ASSIST_SCORE_COOLDOWN_ID = "InfernoA2AssistScoreTimeLimit";

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
        SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_A2_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (i % 4 == 0) {
                Location loc = combatUser.getEntity().getEyeLocation();
                new InfernoA2Area().emit(loc);
            }

            SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_A2_TICK, combatUser.getEntity().getLocation());
            ParticleUtil.play(Particle.FLAME, combatUser.getEntity().getLocation().add(0, 1, 0), 2,
                    0.1, 0.1, 0.1, 0.2);
            playTickEffect(i);

            return true;
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
        Location loc = combatUser.getEntity().getLocation().add(0, 1, 0);
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

                ParticleUtil.play(Particle.SMOKE_NORMAL, loc2, 0, vec.getX(), vec.getY(), vec.getZ(), 0.32);
                ParticleUtil.play(Particle.FLAME, loc2, 0, vec.getX(), vec.getY(), vec.getZ(), 0.2);
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
    }

    private final class InfernoA2Area extends Area {
        private InfernoA2Area() {
            super(combatUser, InfernoA2Info.RADIUS, combatUser::isEnemy);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null,
                    false, true)) {
                target.getStatusEffectModule().applyStatusEffect(combatUser, InfernoA2Burning.instance, 10);
                target.getStatusEffectModule().applyStatusEffect(combatUser, Grounding.getInstance(), 10);
                if (target instanceof CombatUser) {
                    combatUser.addScore("적 고정", (double) (InfernoA2Info.EFFECT_SCORE_PER_SECOND * 4) / 20);
                    CooldownUtil.setCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + target, 10);
                }
            }

            return true;
        }
    }
}
