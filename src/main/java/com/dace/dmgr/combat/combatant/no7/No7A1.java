package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.MeleeHitscan;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class No7A1 extends ActiveSkill {
    public No7A1(@NonNull CombatUser combatUser, @NonNull No7A1Info skillInfo) {
        super(combatUser, skillInfo, No7A1Info.COOLDOWN, No7A1Info.DURATION);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished())
            return null;

        return ActionBarDisplay.builder(this).title().durationBar().keyInfo("해제").build();
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && combatUser.getAbilityManager().getAbility(No7A2Info.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_1;
    }

    @Override
    public void onSlot() {
        if (!isDurationFinished()) {
            cancel();
            return;
        }

        setDuration();
        combatUser.getAbilityManager().getWeapon().cancel();

        HashMap<Damageable, Timestamp> targets = new HashMap<>();

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getLocation().add(0, 1.2, 0);
            double length = combatUser.getEntity().getVelocity().length();

            combatUser.getMoveModule().push(loc.getDirection().multiply(No7A1Info.PUSH), true);

            if (length > No7A1Info.PUSH / 2)
                new No7A1MeleeHitscan(targets).shot(loc);

            No7A1Info.Effects.playTick(combatUser.getLocation());
        }, 1, No7A1Info.DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    private final class No7A1MeleeHitscan extends MeleeHitscan<Damageable> {
        private final HashMap<Damageable, Timestamp> targets;

        private No7A1MeleeHitscan(@NonNull HashMap<Damageable, Timestamp> targets) {
            super(combatUser, EntityCondition.enemy(combatUser), No7A1Info.DISTANCE, No7A1Info.SIZE);
            this.targets = targets;
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return (location, i) -> true;
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                Timestamp damageTimestamp = targets.get(target);

                if (damageTimestamp == null || damageTimestamp.isBefore(Timestamp.now())) {
                    targets.put(target, Timestamp.now().plus(No7A1Info.DAMAGE_COOLDOWN));

                    if (target.getDamageModule().damage(combatUser, No7A1Info.DAMAGE, DamageType.NORMAL, location, false, true)) {
                        combatUser.getAbilityManager().getAbility(No7T1Info.getInstance()).addShield(No7A1Info.SHIELD);

                        if (target instanceof Movable)
                            ((Movable) target).getKnockbackModule().knockback(getVelocity(), No7A1Info.KNOCKBACK);
                    }

                    No7A1Info.Effects.HIT_ENTITY.play(location);
                }

                return false;
            };
        }
    }
}
