package com.dace.dmgr.combat.combatant.vellion.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class VellionP1 extends AbstractSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(VellionP1Info.SPEED);

    public VellionP1(@NonNull CombatUser combatUser) {
        super(combatUser, VellionP1Info.getInstance(), VellionP1Info.COOLDOWN, VellionP1Info.DURATION);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SPACE};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        if (!isCooldownFinished())
            return ActionBarStringUtil.getCooldownBar(this);
        else if (!isDurationFinished())
            return ActionBarStringUtil.getDurationBar(this) + ActionBarStringUtil.getKeyInfo(this, "해제");

        return null;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished()) {
            setDuration();
            combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);
            Location location = combatUser.getLocation();

            VellionP1Info.SOUND.USE.play(location);
            VellionP1Info.PARTICLE.USE.play(location);

            addActionTask(new IntervalTask(i -> {
                Location loc = combatUser.getLocation();

                if (i < 2 && location.distance(loc) > 0) {
                    location.setY(loc.getY());
                    Vector vec = (location.distance(loc) == 0) ? new Vector(0, 0, 0) :
                            LocationUtil.getDirection(location, loc).multiply(VellionP1Info.PUSH_SIDE);
                    vec.setY(VellionP1Info.PUSH_UP);

                    combatUser.getMoveModule().push(vec, true);

                    return false;
                }

                return true;
            }, 1, 2));

            addActionTask(new IntervalTask(i -> {
                if (isDurationFinished() || !combatUser.getEntity().isFlying())
                    return false;

                combatUser.getEntity().setFallDistance(0);
                playTickEffect();

                return true;
            }, isCancelled -> onCancelled(), 1, VellionP1Info.DURATION.toTicks()));
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

        setDuration(Timespan.ZERO);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
        combatUser.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,
                40, -10, false, false), true);

        addTask(new IntervalTask(i -> {
            combatUser.getEntity().setFallDistance(0);

            return !combatUser.getEntity().isOnGround();
        }, () -> combatUser.getEntity().removePotionEffect(PotionEffectType.LEVITATION), 1));

        VellionP1Info.SOUND.DISABLE.play(combatUser.getLocation());
        VellionP1Info.PARTICLE.USE.play(combatUser.getLocation());
    }

    /**
     * 사용 중 효과를 재생한다.
     */
    private void playTickEffect() {
        Location loc = combatUser.getLocation();
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(0.8);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 8; j++) {
            int angle = 360 / 8 * j;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);

            VellionP1Info.PARTICLE.TICK.play(loc.clone().add(vec));
        }
    }
}
