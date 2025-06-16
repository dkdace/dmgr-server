package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public final class No7A3 extends ActiveSkill {
    public No7A3(@NonNull CombatUser combatUser) {
        super(combatUser, No7A3Info.getInstance(), No7A3Info.COOLDOWN, No7A3Info.DURATION, 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return isDurationFinished() ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getActionManager().getSkill(No7A2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getActionManager().getWeapon().cancel();

        Location location = combatUser.getLocation();

        No7A3Info.Effects.USE.play(location);

        long durationTicks = No7A3Info.DURATION.toTicks();

        addActionTask(new IntervalTask(i -> {
            No7A3Area area = new No7A3Area();
            Location loc = combatUser.getLocation().add(0, 0.1, 0);
            area.emit(loc);

            int size = area.getHitTargets().size();
            combatUser.getActionManager().getTrait(No7T1Info.getInstance()).addShield(No7A3Info.SHIELD * (size + 1.0) / durationTicks);

            if (size > 0)
                combatUser.addScore("보호막 획득", (double) (No7A3Info.SHIELD_SCORE * size) / durationTicks);

            No7A3Info.Effects.playTick(i, combatUser.getLocation());
        }, 1, durationTicks));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    private final class No7A3Area extends Area<Damageable> {
        private No7A3Area() {
            super(combatUser, No7A3Info.DETECT_RADIUS, EntityCondition.enemy(combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            return true;
        }
    }
}
