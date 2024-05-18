package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
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
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class NeaceA3 extends ActiveSkill {
    NeaceA3(@NonNull CombatUser combatUser) {
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
        if (isDurationFinished()) {
            new NeaceTarget().shoot();
        } else
            setCooldown();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setCooldown();
    }

    private final class NeaceTarget extends Hitscan {
        private Healable target = null;

        private NeaceTarget() {
            super(combatUser, HitscanOption.builder().size(0.8).maxDistance(NeaceA1Info.MAX_DISTANCE)
                    .condition(combatEntity -> combatEntity instanceof Healable && !combatEntity.isEnemy(NeaceA3.this.combatUser) &&
                            combatEntity != NeaceA3.this.combatUser).build());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            setDuration();

            this.target = (Healable) target;

            SoundUtil.playNamedSound(NamedSound.COMBAT_NEACE_A3_USE, combatUser.getEntity().getLocation());

            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                Location loc = combatUser.getEntity().getLocation().add(0, 0.2, 0);
                Location targetLoc = target.getEntity().getLocation().add(0, 1, 0);
                Vector vec = LocationUtil.getDirection(loc, targetLoc).multiply(NeaceA3Info.PUSH);

                if (!target.canBeTargeted() || target.isDisposed())
                    return false;
                if (targetLoc.distance(loc) < 1.5)
                    return false;
                if (isDurationFinished()) {
                    combatUser.push(vec.clone().multiply(0.5), true);
                    return false;
                }

                if (i == 0)
                    combatUser.push(new Vector(0, 0.5, 0), false);
                else
                    combatUser.push(targetLoc.distance(loc) < 3.5 ? vec.clone().multiply(0.5) : vec, true);
                combatUser.getEntity().setGliding(true);

                ParticleUtil.play(Particle.FIREWORKS_SPARK, loc, 6, 0.2, 0.4, 0.2, 0.1);
                TaskUtil.addTask(NeaceA3.this, new DelayTask(() -> {
                    Location loc2 = combatUser.getEntity().getLocation().add(0, 0.2, 0);
                    for (Location trailLoc : LocationUtil.getLine(loc, loc2, 0.4))
                        ParticleUtil.play(Particle.END_ROD, trailLoc, 1, 0.02, 0.02, 0.02, 0);
                }, 1));

                return true;
            }, isCancelled -> {
                onCancelled();

                combatUser.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,
                        40, -5, false, false), true);

                TaskUtil.addTask(NeaceA3.this, new IntervalTask(i -> {
                    combatUser.getEntity().setFallDistance(0);

                    return !combatUser.getEntity().isOnGround();
                }, isCancelled2 ->
                        combatUser.getEntity().removePotionEffect(PotionEffectType.LEVITATION), 1));
            }, 1, NeaceA3Info.DURATION));

            return false;
        }

        @Override
        protected void onDestroy() {
            if (target == null)
                combatUser.getUser().sendAlert("대상을 찾을 수 없습니다.");
        }
    }
}
