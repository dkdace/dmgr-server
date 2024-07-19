package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.module.statuseffect.Grounding;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public final class InfernoA2 extends ActiveSkill {
    InfernoA2(@NonNull CombatUser combatUser) {
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
    public boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_A2_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (i % 4 == 0) {
                Location loc = combatUser.getEntity().getEyeLocation();
                Predicate<CombatEntity> condition = combatEntity -> combatEntity.isEnemy(combatUser);
                CombatEntity[] targets = CombatUtil.getNearCombatEntities(combatUser.getGame(), loc, InfernoA2Info.RADIUS, condition);
                new InfernoA2Area(condition, targets).emit(loc);
            }

            SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_A2_TICK, combatUser.getEntity().getLocation());
            ParticleUtil.play(Particle.FLAME, combatUser.getEntity().getLocation().add(0, 1, 0), 2,
                    0.1, 0.1, 0.1, 0.2);
            for (int j = 0; j < 3; j++)
                playTickEffect(i * 3 + j);

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
        long yaw = i * 5;
        long pitch = i * 7;

        Location loc = combatUser.getEntity().getLocation().add(0, 1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        Vector vec1 = VectorUtil.getRotatedVector(axis, VectorUtil.getRotatedVector(vector, axis, yaw), pitch + 240);
        Vector vec2 = VectorUtil.getRotatedVector(axis, VectorUtil.getRotatedVector(vector, axis, yaw + 120), pitch + 120);
        Vector vec3 = VectorUtil.getRotatedVector(axis, VectorUtil.getRotatedVector(vector, axis, yaw + 240), pitch);
        ParticleUtil.play(Particle.SMOKE_NORMAL, loc.clone().add(vec1.clone().multiply(1.8)), 0,
                vec1.getX(), vec1.getY(), vec1.getZ(), 0.32);
        ParticleUtil.play(Particle.SMOKE_NORMAL, loc.clone().add(vec2.clone().multiply(1.8)), 0,
                vec2.getX(), vec2.getY(), vec2.getZ(), 0.32);
        ParticleUtil.play(Particle.SMOKE_NORMAL, loc.clone().add(vec3.clone().multiply(1.8)), 0,
                vec3.getX(), vec3.getY(), vec3.getZ(), 0.32);
        ParticleUtil.play(Particle.FLAME, loc.clone().add(vec1.clone().multiply(1.8)), 0,
                vec1.getX(), vec1.getY(), vec1.getZ(), 0.2);
        ParticleUtil.play(Particle.FLAME, loc.clone().add(vec2.clone().multiply(1.8)), 0,
                vec2.getX(), vec2.getY(), vec2.getZ(), 0.2);
        ParticleUtil.play(Particle.FLAME, loc.clone().add(vec3.clone().multiply(1.8)), 0,
                vec3.getX(), vec3.getY(), vec3.getZ(), 0.2);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class InfernoA2Burning extends Burning {
        private static final InfernoA2Burning instance = new InfernoA2Burning();

        @Override
        public void onTick(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider, long i) {
            super.onTick(combatEntity, provider, i);

            if (i % 10 == 0 && combatEntity instanceof Damageable && provider instanceof Attacker &&
                    ((Damageable) combatEntity).getDamageModule().damage((Attacker) provider, InfernoA2Info.FIRE_DAMAGE_PER_SECOND * 10 / 20,
                            DamageType.NORMAL, null, false, true))
                SoundUtil.playNamedSound(NamedSound.COMBAT_DAMAGE_BURNING, combatEntity.getEntity().getLocation());
        }
    }

    private final class InfernoA2Area extends Area {
        private InfernoA2Area(Predicate<CombatEntity> condition, CombatEntity[] targets) {
            super(combatUser, InfernoA2Info.RADIUS, condition, targets);
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
            }

            return true;
        }
    }
}
