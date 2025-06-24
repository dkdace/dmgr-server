package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.skill.Targeted;
import com.dace.dmgr.combat.ability.skill.module.TargetModule;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

@Getter
public final class NeaceA3 extends ActiveSkill implements Targeted<Healable> {
    /** 타겟 모듈 */
    @NonNull
    private final TargetModule<Healable> targetModule;

    public NeaceA3(@NonNull CombatUser combatUser, @NonNull NeaceA3Info skillInfo) {
        super(combatUser, skillInfo, NeaceA3Info.COOLDOWN, NeaceA3Info.DURATION, 2);
        this.targetModule = new TargetModule<>(this, NeaceA3Info.MAX_DISTANCE);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.SLOT_3);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished())
            return null;

        return ActionBarDisplay.builder(this).title().keyInfo("해제").build();
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && (!isDurationFinished() || targetModule.findTarget());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (!isDurationFinished()) {
            combatUser.getMoveModule().push(combatUser.getLocation().getDirection().multiply(0.5), true);
            onEnd();

            return;
        }

        setDuration();

        NeaceA3Info.Effects.USE.play(combatUser.getLocation());

        Healable target = targetModule.getCurrentTarget();

        addActionTask(new IntervalTask(i -> {
            if (!target.canBeTargeted() || target.isRemoved() || combatUser.getKnockbackModule().isKnockbacked())
                return false;

            Location loc = combatUser.getLocation().add(0, 1, 0);
            Location targetLoc = target.getLocation().add(0, 1.5, 0);
            Vector vec = LocationUtil.getDirection(loc, targetLoc).multiply(NeaceA3Info.PUSH);
            double distance = targetLoc.distance(loc);

            if (distance < 1.5)
                return false;

            combatUser.getMoveModule().push(distance < 3.5 ? vec.clone().multiply(0.5) : vec, true);

            addTask(new DelayTask(() -> NeaceA3Info.Effects.playTick(combatUser.getLocation(), loc), 1));

            return true;
        }, isCancelled -> onEnd(), 1, NeaceA3Info.DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    @Override
    @NonNull
    public EntityCondition<Healable> getEntityCondition() {
        return EntityCondition.team(combatUser).exclude(combatUser);
    }

    /**
     * 사용 종료 시 실행할 작업.
     */
    private void onEnd() {
        cancel();

        combatUser.getEntity().addPotionEffect(
                new PotionEffect(PotionEffectType.LEVITATION, 40, -5, false, false), true);

        addTask(new IntervalTask(i -> {
            combatUser.getEntity().setFallDistance(0);

            return !combatUser.getEntity().isOnGround();
        }, () -> combatUser.getEntity().removePotionEffect(PotionEffectType.LEVITATION), 1));
    }
}
