package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
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
        if (!isDurationFinished()) {
            cancel();
            return;
        }

        setDuration();

        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        Location location = combatUser.getLocation();

        VellionP1Info.Sounds.USE.play(location);
        VellionP1Info.Particles.USE.play(location);

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getLocation();

            if (location.distance(loc) > 0) {
                location.setY(loc.getY());

                Vector vec = (location.distance(loc) == 0) ? new Vector(0, 0, 0) : LocationUtil.getDirection(location, loc);
                vec.multiply(VellionP1Info.PUSH_SIDE);
                vec.setY(VellionP1Info.PUSH_UP);

                combatUser.getMoveModule().push(vec, true);

                return false;
            }

            return true;
        }, 1, 2));

        addActionTask(new IntervalTask(i -> {
            if (!combatUser.getEntity().isFlying())
                return false;

            combatUser.getEntity().setFallDistance(0);
            playTickEffect();

            return true;
        }, isCancelled -> cancel(), 1, VellionP1Info.DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
        combatUser.getEntity().addPotionEffect(
                new PotionEffect(PotionEffectType.LEVITATION, 40, -10, false, false), true);

        addTask(new IntervalTask(i -> {
            combatUser.getEntity().setFallDistance(0);

            return !combatUser.getEntity().isOnGround();
        }, () -> combatUser.getEntity().removePotionEffect(PotionEffectType.LEVITATION), 1));

        VellionP1Info.Sounds.DISABLE.play(combatUser.getLocation());
        VellionP1Info.Particles.USE.play(combatUser.getLocation());
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
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

        for (int i = 0; i < 8; i++) {
            int angle = 360 / 8 * i;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);

            VellionP1Info.Particles.TICK.play(loc.clone().add(vec));
        }
    }
}
