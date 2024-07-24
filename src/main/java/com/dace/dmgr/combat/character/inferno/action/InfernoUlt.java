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

    InfernoUlt(@NonNull CombatUser combatUser) {
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
        combatUser.setTemporalHitboxes(new FixedPitchHitbox[]{
                new FixedPitchHitbox(combatUser.getEntity().getLocation(), 2, 2, 2, 0, 1, 0)
        });

        TaskUtil.addTask(this, new IntervalTask(i -> {
            if (combatUser.getDamageModule().getShield(SHIELD_ID) == 0)
                return false;

            if (i < 24) {
                SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_ULT_USE, combatUser.getEntity().getLocation(), 1, i * 0.02);
                ParticleUtil.play(Particle.LAVA, combatUser.getEntity().getLocation().add(0, 1, 0), 3,
                        1, 1.5, 1, 0.2);
                for (int j = 0; j < 3; j++)
                    playUseTickEffect(i * 3 + j);
            }

            if (i % 12 == 0)
                SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_ULT_TICK, combatUser.getEntity().getLocation());
            for (int j = 0; j < 2; j++)
                playTickEffect(i * 2 + j);

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
            combatUser.setTemporalHitboxes(null);
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
        long yaw = i * 6;
        long pitch = i * 5;

        Location loc = combatUser.getEntity().getLocation().add(0, 1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 6; j++) {
            yaw += 60;
            Vector vec1 = VectorUtil.getRotatedVector(axis, VectorUtil.getRotatedVector(vector, axis, yaw), pitch).multiply(2.5);
            Vector vec2 = LocationUtil.getDirection(loc.clone().add(vec1), loc.clone().add(VectorUtil.getRotatedVector(axis,
                    VectorUtil.getRotatedVector(vector, axis, yaw + 5), pitch + 5).multiply(2.5)));

            ParticleUtil.play(Particle.FLAME, loc.clone().add(vec1), 0, vec2.getX(), vec2.getY(), vec2.getZ(), 0.2);
            ParticleUtil.play(Particle.SMOKE_NORMAL, loc.clone().add(vec1), 0, vec2.getX(), vec2.getY(), vec2.getZ(), 0.2);
        }
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playTickEffect(long i) {
        long angle = i * 7;
        double up = i * 0.04 % 1;

        Location loc = combatUser.getEntity().getLocation().add(0, 1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(2);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 4; j++) {
            angle += 90;
            Vector vec1 = VectorUtil.getRotatedVector(vector, axis, angle);
            Vector vec2 = LocationUtil.getDirection(loc.clone().add(vec1), loc.clone().add(VectorUtil.getRotatedVector(vector, axis, angle + 10)));

            ParticleUtil.play(Particle.SMOKE_NORMAL, loc.clone().add(vec1).add(0, up - 0.5, 0), 0,
                    vec2.getX(), vec2.getY(), vec2.getZ(), 0.15);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc.clone().add(vec1), 3, 0, 1, 0,
                    255, 70, 0);
        }
    }
}
