package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class ChedA3 extends ActiveSkill {
    /** 처치 점수 제한시간 쿨타임 ID */
    public static final String KILL_SCORE_COOLDOWN_ID = "ChedA3KillScoreTimeLimit";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "ChedA3";

    public ChedA3(@NonNull CombatUser combatUser) {
        super(combatUser, ChedA3Info.getInstance(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public long getDefaultCooldown() {
        return ChedA3Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && (combatUser.getSkill(ChedP1Info.getInstance()).isDurationFinished() ||
                !combatUser.getEntity().hasGravity());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.setGlobalCooldown((int) ChedA3Info.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -ChedA3Info.READY_SLOW);
        combatUser.getWeapon().onCancelled();
        combatUser.getEntity().getInventory().setItem(30, new ItemStack(Material.AIR));

        SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_A3_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (!combatUser.getSkill(ChedP1Info.getInstance()).isDurationFinished() && combatUser.getEntity().hasGravity())
                return false;

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(true), 0, 0, 1.5);
            playUseTickEffect(loc, i);

            return true;
        }, isCancelled -> {
            onCancelled();
            if (isCancelled)
                return;

            Location location = combatUser.getArmLocation(true);
            new ChedA3Projectile().shoot(location);

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(true), 0, 0, 1.5);
            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                playUseTickEffect(loc, i + ChedA3Info.READY_DURATION);
                return true;
            }, 1, ChedA3Info.READY_DURATION));
            SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_A3_USE_READY, location);
        }, 1, ChedA3Info.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param location 사용 위치
     * @param i        인덱스
     */
    private void playUseTickEffect(@NonNull Location location, long i) {
        Vector vector = VectorUtil.getYawAxis(location);
        Vector axis = VectorUtil.getRollAxis(location);

        for (int j = 0; j < 2; j++) {
            long index = i * 2 + j;
            long angle = index * (i > 12 ? 4 : 8);
            double distance = index * 0.04;
            double forward = 0;
            if (i > 12) {
                forward = (index - 24) * 0.2;
                distance = 48 * 0.04 - index * 0.03;
            }

            for (int k = 0; k < 10; k++) {
                angle += 72;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);
                Vector vec2 = VectorUtil.getRotatedVector(vector, axis, angle + 10.0);
                Vector vec3 = vec.clone().multiply(distance + (k < 5 ? 0 : 1.4));
                Vector dir = LocationUtil.getDirection(location.clone().add(vec), location.clone().add(vec2));
                Location loc2 = location.clone().add(vec3).add(location.getDirection().multiply(forward));

                ParticleUtil.play(Particle.CRIT_MAGIC, loc2, 0, dir.getX(), dir.getY(), dir.getZ(), -0.25);
                ParticleUtil.play(Particle.SMOKE_NORMAL, loc2, 0, dir.getX(), dir.getY(), dir.getZ(), 0.12);
            }
        }
    }

    private final class ChedA3Projectile extends Projectile {
        private ChedA3Projectile() {
            super(combatUser, ChedA3Info.VELOCITY, ProjectileOption.builder().trailInterval(18).size(ChedA3Info.SIZE)
                    .condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            playTickEffect();
            SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_A3_TICK, getLocation());
        }

        private void playTickEffect() {
            Location loc = getLocation().clone();
            loc.setPitch(0);

            ParticleUtil.play(Particle.CRIT_MAGIC, loc, 20, 0.28, 0.28, 0.28, 0);
            ParticleUtil.play(Particle.CRIT_MAGIC, LocationUtil.getLocationFromOffset(loc, 0, -0.5, -0.6), 8,
                    0.2, 0.12, 0.2, 0);
            ParticleUtil.play(Particle.CRIT_MAGIC, LocationUtil.getLocationFromOffset(loc, 0, -0.7, -1.2), 8,
                    0.16, 0.08, 0.16, 0);
            ParticleUtil.play(Particle.CRIT_MAGIC, LocationUtil.getLocationFromOffset(loc, 0, -0.9, -1.8), 8,
                    0.12, 0.04, 0.12, 0);

            ParticleUtil.play(Particle.CRIT_MAGIC, LocationUtil.getLocationFromOffset(loc, 0, 0.4, 0.8), 8,
                    0.1, 0.16, 0.1, 0);
            ParticleUtil.play(Particle.CRIT_MAGIC, LocationUtil.getLocationFromOffset(loc, 0, 0.6, 1), 8,
                    0.1, 0.16, 0.1, 0);
            ParticleUtil.play(Particle.CRIT_MAGIC, LocationUtil.getLocationFromOffset(loc, 0, 0.8, 1.4), 8,
                    0.18, 0.16, 0.18, 0);
            ParticleUtil.play(Particle.CRIT_MAGIC, LocationUtil.getLocationFromOffset(loc, 0, 0.8, 1.6), 8,
                    0.24, 0.16, 0.24, 0);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc, 15, 2.5, 1.5, 2.5,
                    64, 160, 184);

            for (int i = 0; i < 6; i++) {
                ParticleUtil.play(Particle.CRIT_MAGIC, LocationUtil.getLocationFromOffset(loc, 0.7 + i * 0.4,
                        0.3 + i * (i < 3 ? 0.2 : 0.25), 0), 8, 0.1, 0.1 + i * 0.04, 0.1, 0);
                ParticleUtil.play(Particle.CRIT_MAGIC, LocationUtil.getLocationFromOffset(loc, -0.7 - i * 0.4,
                        0.3 + i * (i < 3 ? 0.2 : 0.25), 0), 8, 0.1, 0.1 + i * 0.04, 0.1, 0);
            }
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return true;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.getDamageModule().damage(this, 0, DamageType.NORMAL, getLocation(), false, true)) {
                GlowUtil.setGlowing(target.getEntity(), ChatColor.RED, combatUser.getEntity(), ChedA3Info.DETECT_DURATION);
                if (combatUser.getGameUser() != null && combatUser.getGameUser().getTeam() != null)
                    for (GameUser targetGameUser : combatUser.getGameUser().getTeam().getTeamUsers())
                        GlowUtil.setGlowing(target.getEntity(), ChatColor.RED, targetGameUser.getPlayer(), ChedA3Info.DETECT_DURATION);

                if (target instanceof CombatUser) {
                    combatUser.addScore("적 탐지", ChedA3Info.DETECT_SCORE);
                    CooldownUtil.setCooldown(combatUser, KILL_SCORE_COOLDOWN_ID + target, ChedA3Info.KILL_SCORE_TIME_LIMIT);
                }
            }

            return true;
        }
    }
}
