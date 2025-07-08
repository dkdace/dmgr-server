package com.dace.dmgr.combat.combatant.inferno;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.KnockbackModule;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class InfernoA1 extends ActiveSkill {
    public InfernoA1(@NonNull CombatUser combatUser, @NonNull InfernoA1Info skillInfo) {
        super(combatUser, skillInfo, InfernoA1Info.COOLDOWN, Timespan.MAX);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_1;
    }

    @Override
    public void onSlot() {
        setDuration();

        combatUser.getAbilityManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(InfernoA1Info.GLOBAL_COOLDOWN);

        Location location = combatUser.getLocation();
        location.setPitch(Math.max(-40, Math.min(location.getPitch(), 10)));

        InfernoA1Info.Effects.USE.play(location);

        Vector vec = location.getDirection().multiply(InfernoA1Info.PUSH_SIDE);
        vec.setY(vec.getY() + InfernoA1Info.PUSH_UP);

        combatUser.getMoveModule().push(vec, true);

        addActionTask(new DelayTask(() -> addActionTask(new IntervalTask(i -> {
            if (i < 15)
                InfernoA1Info.Effects.playUseTick(combatUser.getLocation());

            return !combatUser.getEntity().isOnGround();
        }, () -> {
            cancel();
            addActionTask(new DelayTask(() -> {
                Location loc = combatUser.getLocation().add(0, 0.1, 0);
                new InfernoA1Area().emit(loc);

                InfernoA1Info.Effects.playLand(loc);
            }, 1));
        }, 1)), 4));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        if (combatUser.getAbilityManager().getAbility(InfernoUltInfo.getInstance()).isDurationFinished())
            setDuration(Timespan.ZERO);
        else
            setCooldown(getDefaultCooldown().minus(InfernoUltInfo.A1_COOLDOWN_DECREMENT));
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
            if (target.getDamageModule().damage(combatUser, InfernoA1Info.DAMAGE, DamageType.NORMAL, null, false, true))
                KnockbackModule.knockback(target, LocationUtil.getDirection(center, location.clone().add(0, 0.5, 0)), InfernoA1Info.KNOCKBACK);

            InfernoA1Info.Effects.HIT_ENTITY.play(location);

            return !(target instanceof Barrier);
        }
    }
}
