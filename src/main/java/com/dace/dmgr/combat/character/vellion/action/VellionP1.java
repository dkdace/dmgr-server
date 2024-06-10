package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class VellionP1 extends AbstractSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "VellionP1";

    VellionP1(@NonNull CombatUser combatUser) {
        super(combatUser, VellionP1Info.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SPACE};
    }

    @Override
    public long getDefaultCooldown() {
        return VellionP1Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return VellionP1Info.DURATION;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished()) {
            setDuration();
            combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, VellionP1Info.SPEED);
            Location location = combatUser.getEntity().getLocation();

            SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_P1_USE, location);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, combatUser.getEntity().getLocation(), 50,
                    0.8, 0, 0.8, 150, 110, 170);

            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                Location loc = combatUser.getEntity().getLocation();

                if (i < 2 && location.distance(loc) > 0) {
                    location.setY(loc.getY());
                    Vector vec = (location.distance(loc) == 0) ? new Vector(0, 0, 0) :
                            LocationUtil.getDirection(location, loc).multiply(VellionP1Info.PUSH_SIDE);
                    vec.setY(VellionP1Info.PUSH_UP);

                    combatUser.push(vec, true);

                    return false;
                }

                return true;
            }, 1, 2));
            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                if (isDurationFinished())
                    return false;

                combatUser.getEntity().setFallDistance(0);
                playTickEffect();

                return true;
            }, isCancelled -> onCancelled(), 1, VellionP1Info.DURATION));
        } else
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
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        combatUser.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,
                40, -10, false, false), true);

        TaskUtil.addTask(this, new IntervalTask(i -> {
            combatUser.getEntity().setFallDistance(0);

            return !combatUser.getEntity().isOnGround();
        }, isCancelled ->
                combatUser.getEntity().removePotionEffect(PotionEffectType.LEVITATION), 1));

        SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_P1_DISABLE, combatUser.getEntity().getLocation());
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, combatUser.getEntity().getLocation(), 50,
                0.8, 0, 0.8, 150, 110, 170);
    }

    /**
     * 사용 중 효과를 재생한다.
     */
    private void playTickEffect() {
        Location loc = combatUser.getEntity().getLocation();
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(0.8);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 8; j++) {
            int angle = 360 / 8 * j;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc.clone().add(vec),
                    2, 0.3, 0, 0.3, 150, 110, 170);
        }
    }
}
