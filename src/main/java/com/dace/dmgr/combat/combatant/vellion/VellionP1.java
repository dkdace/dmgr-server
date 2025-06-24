package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public final class VellionP1 extends PassiveSkill {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(VellionP1Info.SPEED);

    public VellionP1(@NonNull CombatUser combatUser, @NonNull VellionP1Info skillInfo) {
        super(combatUser, skillInfo, VellionP1Info.COOLDOWN, VellionP1Info.DURATION);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.SPACE);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        ActionBarDisplay.Builder builder = ActionBarDisplay.builder(this).title();

        if (!isCooldownFinished())
            return builder.cooldownBar().build();
        else if (!isDurationFinished())
            return builder.durationBar().keyInfo("해제").build();

        return null;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (!isDurationFinished()) {
            cancel();
            return;
        }

        setDuration();

        combatUser.getMoveModule().addModifier(MODIFIER);

        Location location = combatUser.getLocation();

        VellionP1Info.Effects.USE.play(location);
        VellionP1Info.Effects.ON.play(location);

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

            VellionP1Info.Effects.playTick(combatUser.getLocation());

            return true;
        }, isCancelled -> cancel(), 1, VellionP1Info.DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        combatUser.getMoveModule().removeModifier(MODIFIER);
        combatUser.getEntity().addPotionEffect(
                new PotionEffect(PotionEffectType.LEVITATION, 40, -10, false, false), true);

        addTask(new IntervalTask(i -> {
            combatUser.getEntity().setFallDistance(0);

            return !combatUser.getEntity().isOnGround();
        }, () -> combatUser.getEntity().removePotionEffect(PotionEffectType.LEVITATION), 1));

        VellionP1Info.Effects.USE.play(combatUser.getLocation());
        VellionP1Info.Effects.OFF.play(combatUser.getLocation());
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }
}
