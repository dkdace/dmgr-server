package com.dace.dmgr.combat.combatant.neace.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class NeaceA3 extends ActiveSkill {
    public NeaceA3(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceA3Info.getInstance(), NeaceA3Info.COOLDOWN, NeaceA3Info.DURATION, 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        if (isDurationFinished())
            return null;

        return NeaceA3Info.getInstance() + ActionBarStringUtil.getKeyInfo(this, "해제");
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished())
            new NeaceA3Target().shot();
        else
            cancel();
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    private final class NeaceA3Target extends Target<Healable> {
        private NeaceA3Target() {
            super(combatUser, NeaceA3Info.MAX_DISTANCE, true, CombatUtil.EntityCondition.team(combatUser).exclude(combatUser));
        }

        @Override
        protected void onFindEntity(@NonNull Healable target) {
            setDuration();

            NeaceA3Info.SOUND.USE.play(combatUser.getLocation());

            addActionTask(new IntervalTask(i -> {
                if (!target.canBeTargeted() || target.isRemoved() || combatUser.getMoveModule().isKnockbacked())
                    return false;

                Location loc = combatUser.getLocation().add(0, 1, 0);
                Location targetLoc = target.getLocation().add(0, 1.5, 0);
                Vector vec = LocationUtil.getDirection(loc, targetLoc).multiply(NeaceA3Info.PUSH);

                if (targetLoc.distance(loc) < 1.5)
                    return false;
                if (isDurationFinished()) {
                    combatUser.getMoveModule().push(vec.multiply(0.5), true);
                    return false;
                }

                combatUser.getMoveModule().push(targetLoc.distance(loc) < 3.5 ? vec.clone().multiply(0.5) : vec, true);

                NeaceA3Info.PARTICLE.TICK_CORE.play(loc);

                addTask(new DelayTask(() -> {
                    Location loc2 = combatUser.getLocation().add(0, 1, 0);
                    for (Location loc3 : LocationUtil.getLine(loc, loc2, 0.4))
                        NeaceA3Info.PARTICLE.TICK_DECO.play(loc3);
                }, 1));

                return true;
            }, isCancelled -> {
                cancel();

                combatUser.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,
                        40, -5, false, false), true);

                addTask(new IntervalTask(i -> {
                    combatUser.getEntity().setFallDistance(0);

                    return !combatUser.getEntity().isOnGround();
                }, () -> combatUser.getEntity().removePotionEffect(PotionEffectType.LEVITATION), 1));
            }, 1, NeaceA3Info.DURATION.toTicks()));
        }
    }
}
