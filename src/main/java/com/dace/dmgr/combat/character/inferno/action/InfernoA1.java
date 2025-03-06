package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class InfernoA1 extends ActiveSkill {
    public InfernoA1(@NonNull CombatUser combatUser) {
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
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown(Timespan.ofTicks(InfernoA1Info.GLOBAL_COOLDOWN));

        Location location = combatUser.getLocation();
        location.setPitch(Math.max(-40, Math.min(location.getPitch(), 10)));

        InfernoA1Info.SOUND.USE.play(location);
        InfernoA1Info.PARTICLE.USE.play(location);

        Vector vec = location.getDirection().multiply(InfernoA1Info.PUSH_SIDE);
        vec.setY(vec.getY() + InfernoA1Info.PUSH_UP);

        combatUser.getMoveModule().push(vec, true);

        TaskUtil.addTask(taskRunner, new DelayTask(() -> TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (i < 15) {
                Location loc = combatUser.getLocation();
                loc.setPitch(0);

                for (int j = 0; j < 2; j++) {
                    Location loc2 = LocationUtil.getLocationFromOffset(loc, -0.3 + j * 0.6, 0.8, -0.5);
                    InfernoA1Info.PARTICLE.USE_TICK.play(loc2);
                }
            }

            return !combatUser.getEntity().isOnGround();
        }, () -> {
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
        Location loc = combatUser.getLocation().add(0, 0.1, 0);
        new InfernoA1Area().emit(loc);

        InfernoA1Info.SOUND.LAND.play(loc);
        Block floor = loc.clone().subtract(0, 0.5, 0).getBlock();
        CombatEffectUtil.playHitBlockParticle(loc, floor, 5);
        InfernoA1Info.PARTICLE.LAND_CORE.play(loc);

        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(0.8);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int i = 0; i < 18; i++) {
            int angle = 360 / 18 * i;
            Vector vec = VectorUtil.getSpreadedVector(VectorUtil.getRotatedVector(vector, axis, angle), 8);
            Location loc2 = loc.clone().add(vec.clone().multiply(1.5));
            Location loc3 = loc.clone().add(vec);

            InfernoA1Info.PARTICLE.LAND_DECO_1.play(loc2);
            for (int j = 0; j < 2; j++)
                InfernoA1Info.PARTICLE.LAND_DECO_2.play(loc3, vec.setY(vec.getY() + 0.1));
        }
    }

    private final class InfernoA1Area extends Area<Damageable> {
        private InfernoA1Area() {
            super(combatUser, InfernoA1Info.RADIUS, CombatUtil.EntityCondition.enemy(combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, InfernoA1Info.DAMAGE, DamageType.NORMAL, null, false, true)
                    && target instanceof Movable)
                ((Movable) target).getMoveModule().knockback(LocationUtil.getDirection(center, location.clone().add(0, 0.5, 0))
                        .multiply(InfernoA1Info.KNOCKBACK));

            InfernoA1Info.PARTICLE.HIT_ENTITY.play(location);

            return !(target instanceof Barrier);
        }
    }
}
