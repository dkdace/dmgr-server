package com.dace.dmgr.combat.combatant.inferno.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class InfernoUlt extends UltimateSkill {
    /** 보호막 */
    @Nullable
    private DamageModule.Shield shield;

    public InfernoUlt(@NonNull CombatUser combatUser) {
        super(combatUser, InfernoUltInfo.getInstance(), InfernoUltInfo.DURATION, InfernoUltInfo.COST);
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return isDurationFinished() ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(InfernoA1Info.getInstance()).isDurationFinished()
                && combatUser.getSkill(InfernoA2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();
        combatUser.getSkill(InfernoA1Info.getInstance()).setCooldown(Timespan.ZERO);

        InfernoWeapon weapon = (InfernoWeapon) combatUser.getWeapon();
        weapon.cancel();
        weapon.getReloadModule().resetRemainingAmmo();

        combatUser.setHitboxes(Hitbox.builder(2, 2, 2).offsetY(1).pitchFixed().build());

        shield = combatUser.getDamageModule().createShield(InfernoUltInfo.SHIELD);

        addActionTask(new IntervalTask(i -> {
            if (shield != null && shield.getHealth() == 0)
                return false;

            playTickEffect(i);
            Location loc = combatUser.getLocation();
            if (i < 24)
                InfernoUltInfo.SOUND.USE.play(loc, 1, i / 23.0);
            if (i % 12 == 0)
                InfernoUltInfo.SOUND.TICK.play(loc);

            return true;
        }, isCancelled -> {
            if (!isCancelled)
                return;

            setDuration(Timespan.ZERO);

            Location loc = combatUser.getLocation();
            InfernoUltInfo.SOUND.DEATH.play(loc);
            InfernoUltInfo.PARTICLE.DEATH.play(loc);
        }, 1, InfernoUltInfo.DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        combatUser.resetHitboxes();

        if (shield != null) {
            shield.setHealth(0);
            shield = null;
        }
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playTickEffect(long i) {
        if (i < 24)
            playUseTickEffect(i);

        Location loc = combatUser.getLocation().add(0, 1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(2);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 2; j++) {
            long index = i * 2 + j;
            long angle = index * 7;
            double up = index * 0.04 % 1 - 0.5;

            for (int k = 0; k < 4; k++) {
                angle += 360 / 4;
                Vector vec1 = VectorUtil.getRotatedVector(vector, axis, angle);
                Vector vec2 = VectorUtil.getRotatedVector(vector, axis, angle + 10.0);

                Location loc2 = loc.clone().add(vec1);
                Vector dir = LocationUtil.getDirection(loc.clone().add(vec1), loc.clone().add(vec2));

                InfernoUltInfo.PARTICLE.TICK_CORE.play(loc2);
                InfernoUltInfo.PARTICLE.TICK_DECO.play(loc2.clone().add(0, up, 0), dir);
            }
        }
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playUseTickEffect(long i) {
        Location loc = combatUser.getLocation().add(0, 1, 0);
        loc.setYaw(0);
        loc.setPitch(0);

        InfernoUltInfo.PARTICLE.USE_TICK_CORE.play(loc);

        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 3; j++) {
            long index = i * 3 + j;
            long yaw = index * 6;
            long pitch = index * 5;

            for (int k = 0; k < 5; k++) {
                yaw += 360 / 5;
                Vector vec1 = VectorUtil.getRotatedVector(axis, VectorUtil.getRotatedVector(vector, axis, yaw), pitch);
                Vector vec2 = VectorUtil.getRotatedVector(axis, VectorUtil.getRotatedVector(vector, axis, yaw + 10.0), pitch + 10.0);

                Location loc2 = loc.clone().add(vec1.clone().multiply(2.5));
                Vector dir = LocationUtil.getDirection(loc.clone().add(vec1), loc.clone().add(vec2));

                InfernoUltInfo.PARTICLE.USE_TICK_DECO.play(loc2, dir);
            }
        }
    }
}
