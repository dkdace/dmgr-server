package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.character.neace.Neace;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class NeaceA3 extends ActiveSkill {
    public NeaceA3(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceA3Info.getInstance(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public long getDefaultCooldown() {
        return NeaceA3Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return NeaceA3Info.DURATION;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished())
            new NeaceA3Target().shoot();
        else
            onCancelled();
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

    private final class NeaceA3Target extends Target {
        private NeaceA3Target() {
            super(combatUser, NeaceA3Info.MAX_DISTANCE, true, combatEntity -> Neace.getTargetedActionCondition(NeaceA3.this.combatUser, combatEntity));
        }

        @Override
        protected void onFindEntity(@NonNull Damageable target) {
            setDuration();

            SoundUtil.playNamedSound(NamedSound.COMBAT_NEACE_A3_USE, combatUser.getEntity().getLocation());

            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                if (!target.canBeTargeted() || target.isDisposed() || combatUser.getKnockbackModule().isKnockbacked())
                    return false;

                Location loc = combatUser.getEntity().getLocation().add(0, 1, 0);
                Location targetLoc = target.getEntity().getLocation().add(0, 1.5, 0);
                Vector vec = LocationUtil.getDirection(loc, targetLoc).multiply(NeaceA3Info.PUSH);

                if (targetLoc.distance(loc) < 1.5)
                    return false;
                if (isDurationFinished()) {
                    combatUser.getMoveModule().push(vec.multiply(0.5), true);
                    return false;
                }

                combatUser.getMoveModule().push(targetLoc.distance(loc) < 3.5 ? vec.clone().multiply(0.5) : vec, true);

                ParticleUtil.play(Particle.FIREWORKS_SPARK, loc, 6, 0.2, 0.4, 0.2, 0.1);

                TaskUtil.addTask(NeaceA3.this, new DelayTask(() -> {
                    Location loc2 = combatUser.getEntity().getLocation().add(0, 1, 0);
                    for (Location loc3 : LocationUtil.getLine(loc, loc2, 0.4))
                        ParticleUtil.play(Particle.END_ROD, loc3, 1, 0.02, 0.02, 0.02, 0);
                }, 1));

                return true;
            }, isCancelled -> {
                onCancelled();

                combatUser.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,
                        40, -5, false, false), true);

                TaskUtil.addTask(NeaceA3.this, new IntervalTask(i -> {
                    combatUser.getEntity().setFallDistance(0);

                    return !combatUser.getEntity().isOnGround();
                }, () -> combatUser.getEntity().removePotionEffect(PotionEffectType.LEVITATION), 1));
            }, 1, NeaceA3Info.DURATION));
        }
    }
}
