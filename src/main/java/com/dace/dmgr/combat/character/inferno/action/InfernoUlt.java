package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.interaction.FixedPitchHitbox;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

@Getter
public final class InfernoUlt extends UltimateSkill {
    /** 보호막 ID */
    private static final String SHIELD_ID = "InfernoUlt";

    public InfernoUlt(@NonNull CombatUser combatUser) {
        super(combatUser, InfernoUltInfo.getInstance());
    }

    @Override
    public int getCost() {
        return InfernoUltInfo.COST;
    }

    @Override
    public long getDefaultDuration() {
        return InfernoUltInfo.DURATION;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && combatUser.getSkill(InfernoA1Info.getInstance()).isDurationFinished() &&
                combatUser.getSkill(InfernoA2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();
        combatUser.getWeapon().onCancelled();
        ((InfernoWeapon) combatUser.getWeapon()).getReloadModule().setRemainingAmmo(InfernoWeaponInfo.CAPACITY);
        combatUser.getSkill(InfernoA1Info.getInstance()).setCooldown(0);
        combatUser.getDamageModule().setShield(SHIELD_ID, InfernoUltInfo.HEALTH);
        combatUser.setTemporaryHitboxes(new FixedPitchHitbox[]{
                new FixedPitchHitbox(combatUser.getEntity().getLocation(), 2, 2, 2, 0, 1, 0)
        });

        TaskUtil.addTask(this, new IntervalTask(i -> {
            if (combatUser.getDamageModule().getShield(SHIELD_ID) == 0)
                return false;

            if (i < 24) {
                SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_ULT_USE, combatUser.getEntity().getLocation(), 1, i * 0.02);
                ParticleUtil.play(Particle.LAVA, combatUser.getEntity().getLocation().add(0, 1, 0), 3,
                        1, 1.5, 1, 0.2);
                playUseTickEffect(i);
            }

            playTickEffect(i);
            if (i % 12 == 0)
                SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_ULT_TICK, combatUser.getEntity().getLocation());

            return true;
        }, isCancelled -> {
            if (isCancelled) {
                setDuration(0);

                SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_ULT_DEATH, combatUser.getEntity().getLocation());
                ParticleUtil.play(Particle.FLAME, combatUser.getEntity().getLocation(), 300, 0.4, 0.4, 0.4, 0.2);
                ParticleUtil.play(Particle.SMOKE_NORMAL, combatUser.getEntity().getLocation(), 250, 0.3, 0.3, 0.3, 0.25);
                ParticleUtil.play(Particle.SMOKE_LARGE, combatUser.getEntity().getLocation(), 150, 0.4, 0.4, 0.4, 0.2);
            }

            combatUser.getDamageModule().setShield(SHIELD_ID, 0);
            combatUser.setTemporaryHitboxes(null);
        }, 1, InfernoUltInfo.DURATION));
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playUseTickEffect(long i) {
        Location loc = combatUser.getEntity().getLocation().add(0, 1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 3; j++) {
            long index = i * 3 + j;
            long yaw = index * 6;
            long pitch = index * 5;

            for (int k = 0; k < 6; k++) {
                yaw += 60;

                Vector vec1 = VectorUtil.getRotatedVector(axis, VectorUtil.getRotatedVector(vector, axis, yaw), pitch);
                Vector vec2 = VectorUtil.getRotatedVector(axis, VectorUtil.getRotatedVector(vector, axis, yaw + 10.0), pitch + 10.0);
                Vector dir = LocationUtil.getDirection(loc.clone().add(vec1), loc.clone().add(vec2));
                Location loc2 = loc.clone().add(vec1.clone().multiply(2.5));

                ParticleUtil.play(Particle.FLAME, loc2, 0, dir.getX(), dir.getY(), dir.getZ(), 0.2);
                ParticleUtil.play(Particle.SMOKE_NORMAL, loc2, 0, dir.getX(), dir.getY(), dir.getZ(), 0.2);
            }
        }
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playTickEffect(long i) {
        Location loc = combatUser.getEntity().getLocation().add(0, 1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(2);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 2; j++) {
            long index = i * 2 + j;
            long angle = index * 7;
            double up = index * 0.04 % 1;

            for (int k = 0; k < 4; k++) {
                angle += 90;
                Vector vec1 = VectorUtil.getRotatedVector(vector, axis, angle);
                Vector vec2 = VectorUtil.getRotatedVector(vector, axis, angle + 10.0);
                Vector dir = LocationUtil.getDirection(loc.clone().add(vec1), loc.clone().add(vec2));
                Location loc2 = loc.clone().add(vec1);

                ParticleUtil.play(Particle.SMOKE_NORMAL, loc2.clone().add(0, up - 0.5, 0), 0,
                        dir.getX(), dir.getY(), dir.getZ(), 0.15);
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc2, 3, 0, 1, 0,
                        255, 70, 0);
            }
        }
    }
}
