package com.dace.dmgr.combat.combatant.inferno;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class InfernoA1 extends ActiveSkill {
    public InfernoA1(@NonNull CombatUser combatUser) {
        super(combatUser, InfernoA1Info.getInstance(), InfernoA1Info.COOLDOWN, Timespan.MAX, 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.getActionManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(InfernoA1Info.GLOBAL_COOLDOWN);

        Location location = combatUser.getLocation();
        location.setPitch(Math.max(-40, Math.min(location.getPitch(), 10)));

        InfernoA1Info.Sounds.USE.play(location);
        InfernoA1Info.Particles.USE.play(location);

        Vector vec = location.getDirection().multiply(InfernoA1Info.PUSH_SIDE);
        vec.setY(vec.getY() + InfernoA1Info.PUSH_UP);

        combatUser.getMoveModule().push(vec, true);

        addActionTask(new DelayTask(() -> addActionTask(new IntervalTask(i -> {
            if (i < 15) {
                Location loc = combatUser.getLocation();
                loc.setPitch(0);

                for (int j = 0; j < 2; j++) {
                    Location loc2 = LocationUtil.getLocationFromOffset(loc, -0.3 + j * 0.6, 0.8, -0.5);
                    InfernoA1Info.Particles.USE_TICK.play(loc2);
                }
            }

            return !combatUser.getEntity().isOnGround();
        }, () -> {
            cancel();
            addActionTask(new DelayTask(this::onLand, 1));
        }, 1)), 4));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        if (combatUser.getActionManager().getSkill(InfernoUltInfo.getInstance()).isDurationFinished())
            setDuration(Timespan.ZERO);
        else
            setCooldown(getDefaultCooldown().minus(InfernoUltInfo.A1_COOLDOWN_DECREMENT));
    }

    /**
     * 점프 후 착지 시 실행할 작업.
     */
    private void onLand() {
        Location loc = combatUser.getLocation().add(0, 0.1, 0);
        new InfernoA1Area().emit(loc);

        InfernoA1Info.Sounds.LAND.play(loc);
        InfernoA1Info.Particles.LAND_CORE.play(loc);
        CombatEffectUtil.playHitBlockParticle(loc, loc.clone().subtract(0, 0.5, 0).getBlock(), 5);

        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(0.8);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int i = 0; i < 18; i++) {
            int angle = 360 / 18 * i;
            Vector vec = VectorUtil.getSpreadedVector(VectorUtil.getRotatedVector(vector, axis, angle), 8);
            Location loc2 = loc.clone().add(vec.clone().multiply(1.5));
            Location loc3 = loc.clone().add(vec);

            InfernoA1Info.Particles.LAND_DECO_1.play(loc2);
            for (int j = 0; j < 2; j++)
                InfernoA1Info.Particles.LAND_DECO_2.play(loc3, vec.setY(vec.getY() + 0.1));
        }
    }

    private final class InfernoA1Area extends Area<Damageable> {
        private InfernoA1Area() {
            super(combatUser, InfernoA1Info.RADIUS, EntityCondition.enemy(combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, InfernoA1Info.DAMAGE, DamageType.NORMAL, null, false, true)
                    && target instanceof Movable) {
                Vector dir = LocationUtil.getDirection(center, location.clone().add(0, 0.5, 0)).multiply(InfernoA1Info.KNOCKBACK);
                ((Movable) target).getMoveModule().knockback(dir);
            }

            InfernoA1Info.Particles.HIT_ENTITY.play(location);
            return !(target instanceof Barrier);
        }
    }
}
