package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.character.silia.SiliaTrait;
import com.dace.dmgr.combat.entity.Barrier;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public final class SiliaA1 extends ActiveSkill {
    public SiliaA1(@NonNull CombatUser combatUser) {
        super(1, combatUser, SiliaA1Info.getInstance(), 0);
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
        return super.canUse() && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getWeapon().setCooldown(0);
        combatUser.setGlobalCooldown(6);
        setDuration(-1);
        combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.USE);
        combatUser.playMeleeAttackAnimation(-3, 6, true);
        playUseSound(combatUser.getEntity().getLocation());

        Location location = combatUser.getEntity().getLocation();
        Set<CombatEntity> targets = new HashSet<>();
        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.5, 0);
            combatUser.push(loc.getDirection().multiply(2.5), true);

            new SiliaA1Hitscan(targets).shoot();
            CombatUtil.setYawAndPitch(combatUser.getEntity(), location.getYaw(), location.getPitch());

            TaskUtil.addTask(combatUser, new DelayTask(() -> {
                Location loc2 = combatUser.getEntity().getEyeLocation().subtract(0, 0.5, 0);
                for (Location trailLoc : LocationUtil.getLine(loc, loc2, 0.3)) {
                    ParticleUtil.play(Particle.CRIT, trailLoc, 3, 0.02, 0.02, 0.02, 0);
                    ParticleUtil.play(Particle.END_ROD, trailLoc, 1, 0.02, 0.02, 0.02, 0);
                }
            }, 1));

            return true;
        }, isCancelled -> {
            combatUser.push(new Vector(), true);
            onCancelled();
        }, 1, 6));
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        if (!isDurationFinished())
            setDuration(0);

        if (((SiliaWeapon) combatUser.getWeapon()).isStrike)
            combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.EXTENDED);
        else
            combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.DEFAULT);
    }

    /**
     * 사용 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playUseSound(Location location) {
        SoundUtil.play("new.item.trident.throw", location, 1.5, 0.8);
        SoundUtil.play("random.swordhit", location, 1.5, 0.8);
        SoundUtil.play("random.swordhit", location, 1.5, 0.8);
    }

    private class SiliaA1Hitscan extends Hitscan {
        private final Set<CombatEntity> targets;

        public SiliaA1Hitscan(Set<CombatEntity> targets) {
            super(SiliaA1.this.combatUser, HitscanOption.builder().trailInterval(12).maxDistance(3).condition(SiliaA1.this.combatUser::isEnemy).build());
            this.targets = targets;
        }

        @Override
        protected void trail(@NonNull Location location, @NonNull Vector direction) {
            for (int i = 0; i < 12; i++) {
                Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.2, 1);
                Vector vector = VectorUtil.getPitchAxis(loc).multiply(1.5);
                Vector axis = VectorUtil.getYawAxis(loc);

                Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 15 * (i - 5.5));
                for (int j = 0; j < 3; j++) {
                    Location trailLoc = LocationUtil.getLocationFromOffset(loc.clone().add(vec), 0, 0.3 - j * 0.3, 0);
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc, 1, 0, 0, 0,
                            255, 255, 255);

                    if ((i == 0 || i == 11) && j == 1) {
                        Vector vec2 = VectorUtil.getSpreadedVector(direction, 10);
                        ParticleUtil.play(Particle.EXPLOSION_NORMAL, trailLoc, 0, -vec2.getX(), -vec2.getY(), -vec2.getZ(), 0.4);
                    }
                }
            }
            CombatEntity[] areaTargets = CombatUtil.getNearCombatEntities(combatUser.getGame(), location, SiliaA1Info.RADIUS, condition);

            new SiliaA1Area(condition, areaTargets).emit(location);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location location, @NonNull Vector velocity, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit) {
            return true;
        }

        private class SiliaA1Area extends Area {
            private SiliaA1Area(Predicate<CombatEntity> condition, CombatEntity[] targets) {
                super(SiliaA1.this.combatUser, SiliaA1Info.RADIUS, condition, targets);
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (SiliaA1Hitscan.this.targets.add(target)) {
                    target.getDamageModule().damage(combatUser, SiliaA1Info.DAMAGE, DamageType.NORMAL, null,
                            SiliaTrait.isBackAttack(LocationUtil.getDirection(center, location), target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true);
                    ParticleUtil.play(Particle.CRIT, location, 40, 0, 0, 0, 0.4);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
