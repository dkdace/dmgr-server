package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.temporal.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public final class InfernoA1 extends ActiveSkill {
    InfernoA1(@NonNull CombatUser combatUser) {
        super(combatUser, InfernoA1Info.getInstance(), 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public long getDefaultCooldown() {
        return InfernoA1Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown(InfernoA1Info.GLOBAL_COOLDOWN);

        Location location = combatUser.getEntity().getLocation();
        location.setPitch(Math.max(-40, Math.min(location.getPitch(), 10)));

        SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_A1_USE, location);
        ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 80, 0.5, 0.2, 0.5, 0.2);

        Vector vec = location.getDirection().multiply(InfernoA1Info.PUSH_SIDE);
        vec.setY(vec.getY() + InfernoA1Info.PUSH_UP);

        combatUser.push(vec, true);

        TaskUtil.addTask(taskRunner, new DelayTask(() -> TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (i < 15) {
                Location loc = combatUser.getEntity().getLocation();
                loc.setPitch(0);

                for (int j = 0; j < 2; j++) {
                    Location loc2 = LocationUtil.getLocationFromOffset(loc, -0.3 + j * 0.6, 0.8, -0.5);
                    ParticleUtil.play(Particle.FLAME, loc2, 4, 0, 0.15, 0, 0.02);
                    ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc2, 0, 0, -1, 0, 0.3);
                }
            }

            return !combatUser.getEntity().isOnGround();
        }, isCancelled -> {
            onCancelled();
            TaskUtil.addTask(this, new DelayTask(this::onLand, 1));
        }, 1)), 4));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        if (!combatUser.getSkill(InfernoUltInfo.getInstance()).isDurationFinished())
            setCooldown(getDefaultCooldown() - InfernoUltInfo.A1_COOLDOWN_DECREMENT);
    }

    /**
     * 점프 후 착지 시 실행할 작업.
     */
    private void onLand() {
        Location loc = combatUser.getEntity().getLocation().add(0, 0.1, 0);
        Predicate<CombatEntity> condition = combatEntity -> combatEntity.isEnemy(combatUser);
        CombatEntity[] targets = CombatUtil.getNearCombatEntities(combatUser.getGame(), loc, InfernoA1Info.RADIUS, condition);
        new InfernoA1Area(condition, targets).emit(loc);

        SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_A1_LAND, loc);
        Block floor = loc.clone().subtract(0, 0.5, 0).getBlock();
        CombatUtil.playBlockHitEffect(loc, floor, 5);
        ParticleUtil.play(Particle.SMOKE_NORMAL, loc, 200, 0.8, 0.1, 0.8, 0.05);

        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(0.8);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 18; j++) {
            int angle = 360 / 18 * j;
            Vector vec = VectorUtil.getSpreadedVector(VectorUtil.getRotatedVector(vector, axis, angle), 8);
            Location loc2 = loc.clone().add(vec.clone().multiply(1.5));
            Location loc3 = loc.clone().add(vec);

            ParticleUtil.play(Particle.FLAME, loc2, 5, 0, 0, 0, 0.05);
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc3, 0, vec.getX(), vec.getY() + 0.1, vec.getZ(), 0.35);
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc3, 0, vec.getX(), vec.getY() + 0.2, vec.getZ(), 0.35);
        }
    }

    private final class InfernoA1Area extends Area {
        private InfernoA1Area(Predicate<CombatEntity> condition, CombatEntity[] targets) {
            super(combatUser, InfernoA1Info.RADIUS, condition, targets);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, InfernoA1Info.DAMAGE, DamageType.NORMAL, null, false, true))
                target.getKnockbackModule().knockback(LocationUtil.getDirection(center, location.add(0, 0.5, 0))
                        .multiply(InfernoA1Info.KNOCKBACK));
            ParticleUtil.play(Particle.CRIT, location, 50, 0, 0, 0, 0.4);

            return !(target instanceof Barrier);
        }
    }

}
