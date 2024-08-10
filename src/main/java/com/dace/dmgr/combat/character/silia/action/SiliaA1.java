package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.function.Predicate;

public final class SiliaA1 extends ActiveSkill {
    public SiliaA1(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaA1Info.getInstance(), 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public long getDefaultCooldown() {
        return SiliaA1Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && combatUser.getSkill(SiliaP2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().setCooldown(0);
        combatUser.getWeapon().setVisible(false);
        combatUser.setGlobalCooldown((int) SiliaA1Info.DURATION);
        combatUser.playMeleeAttackAnimation(-3, 6, true);

        Location location = combatUser.getEntity().getEyeLocation().subtract(0, 0.5, 0);

        SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_A1_USE, location);

        HashSet<CombatEntity> targets = new HashSet<>();

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.5, 0);
            combatUser.getMoveModule().push(location.getDirection().multiply(SiliaA1Info.PUSH), true);

            new SiliaA1Attack(targets).shoot();

            CombatUtil.setYawAndPitch(combatUser.getEntity(), location.getYaw(), location.getPitch());

            TaskUtil.addTask(SiliaA1.this, new DelayTask(() -> {
                Location loc2 = combatUser.getEntity().getEyeLocation().subtract(0, 0.5, 0);
                for (Location loc3 : LocationUtil.getLine(loc, loc2, 0.3)) {
                    ParticleUtil.play(Particle.CRIT, loc3, 3, 0.02, 0.02, 0.02, 0);
                    ParticleUtil.play(Particle.END_ROD, loc3, 1, 0.02, 0.02, 0.02, 0);
                }
            }, 1));

            return true;
        }, isCancelled -> {
            onCancelled();
            combatUser.getMoveModule().push(new Vector(), true);
        }, 1, SiliaA1Info.DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        if (!isDurationFinished())
            setDuration(0);
        combatUser.getWeapon().setVisible(true);
    }

    private final class SiliaA1Attack extends Hitscan {
        private final HashSet<CombatEntity> targets;

        private SiliaA1Attack(HashSet<CombatEntity> targets) {
            super(combatUser, HitscanOption.builder().trailInterval(12).maxDistance(SiliaA1Info.DISTANCE).condition(combatUser::isEnemy).build());
            this.targets = targets;
        }

        @Override
        protected void onTrailInterval() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0, -0.2, 1);
            Vector vector = VectorUtil.getPitchAxis(loc).multiply(1.5);
            Vector axis = VectorUtil.getYawAxis(loc);

            for (int i = 0; i < 12; i++) {
                Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 15 * (i - 5.5));
                for (int j = 0; j < 3; j++) {
                    Location loc2 = LocationUtil.getLocationFromOffset(loc.clone().add(vec), 0, 0.3 - j * 0.3, 0);
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc2, 1, 0, 0, 0,
                            255, 255, 255);

                    if ((i == 0 || i == 11) && j == 1) {
                        Vector vec2 = VectorUtil.getSpreadedVector(getVelocity().clone().normalize(), 10);
                        ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc2, 0, -vec2.getX(), -vec2.getY(), -vec2.getZ(), 0.4);
                    }
                }
            }

            new SiliaA1Area(condition).emit(getLocation());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            return true;
        }

        private final class SiliaA1Area extends Area {
            private SiliaA1Area(Predicate<CombatEntity> condition) {
                super(combatUser, SiliaA1Info.RADIUS, condition);
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (targets.add(target)) {
                    target.getDamageModule().damage(combatUser, SiliaA1Info.DAMAGE, DamageType.NORMAL, null,
                            SiliaT1.isBackAttack(LocationUtil.getDirection(center, location), target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true);
                    ParticleUtil.play(Particle.CRIT, location, 40, 0, 0, 0, 0.4);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
