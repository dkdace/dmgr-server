package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class No7A1 extends ActiveSkill {
    public No7A1(@NonNull CombatUser combatUser) {
        super(combatUser, No7A1Info.getInstance(), No7A1Info.COOLDOWN, No7A1Info.DURATION, 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        if (isDurationFinished())
            return null;

        return ActionBarStringUtil.getDurationBar(this) + ActionBarStringUtil.getKeyInfo(this, "해제");
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (!isDurationFinished()) {
            cancel();
            return;
        }

        setDuration();
        combatUser.getActionManager().getWeapon().cancel();

        HashMap<Damageable, Timestamp> targets = new HashMap<>();

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getLocation().add(0, 1.2, 0);
            double length = combatUser.getEntity().getVelocity().length();

            combatUser.getMoveModule().push(loc.getDirection().multiply(No7A1Info.PUSH), true);

            if (length > No7A1Info.PUSH / 2)
                new No7A1Attack(targets).shot(loc);

            No7A1Info.Sounds.TICK.play(combatUser.getLocation());
            for (int j = 0; j < 12; j++) {
                Location loc2 = LocationUtil.getLocationFromOffset(loc, 0, 0, -0.3);
                Vector vec = VectorUtil.getSpreadedVector(loc.getDirection().multiply(-1), 60);

                No7A1Info.Particles.TICK.play(loc2, vec, Math.random());
            }
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

    private final class No7A1Attack extends Hitscan<Damageable> {
        private final HashMap<Damageable, Timestamp> targets;

        private No7A1Attack(@NonNull HashMap<Damageable, Timestamp> targets) {
            super(combatUser, EntityCondition.enemy(combatUser), Option.builder().size(No7A1Info.SIZE).maxDistance(No7A1Info.DISTANCE).build());
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
                    if (target.getDamageModule().damage(combatUser, No7A1Info.DAMAGE, DamageType.NORMAL, location, false, true)) {
                        targets.put(target, Timestamp.now().plus(No7A1Info.DAMAGE_COOLDOWN));
                        combatUser.getActionManager().getTrait(No7T1Info.getInstance()).addShield(No7A1Info.SHIELD);

                        if (target instanceof Movable)
                            ((Movable) target).getMoveModule().knockback(getVelocity().normalize().multiply(No7A1Info.KNOCKBACK));
                    }

                    No7A1Info.Sounds.HIT_ENTITY.play(location);
                    No7A1Info.Particles.HIT_ENTITY.play(location);
                }

                return false;
            };
        }
    }
}
