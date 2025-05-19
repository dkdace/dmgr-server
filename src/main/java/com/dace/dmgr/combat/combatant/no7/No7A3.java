package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
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

        No7A3Info.Sounds.USE.play(location);

        long durationTicks = No7A3Info.DURATION.toTicks();

        addActionTask(new IntervalTask(i -> {
            No7A3Area area = new No7A3Area();
            Location loc = combatUser.getLocation().add(0, 0.1, 0);
            area.emit(loc);

            int size = area.getHitTargets().size();
            combatUser.getActionManager().getTrait(No7T1Info.getInstance()).addShield(No7A3Info.SHIELD * (size + 1.0) / durationTicks);

            if (size > 0)
                combatUser.addScore("보호막 획득", (double) (No7A3Info.SHIELD_SCORE * size) / durationTicks);

            No7A3Info.Sounds.TICK.play(loc);
            playTickEffect(i);
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

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playTickEffect(long i) {
        Location loc = combatUser.getLocation().add(0, 1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(3 - i * 0.1);
        Vector axis = VectorUtil.getYawAxis(loc);

        long angle = i * 4;
        for (int j = 0; j < 6; j++) {
            angle += 360 / 6;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);
            Location loc2 = loc.clone().add(vec);

            No7A3Info.Particles.TICK_CORE.play(loc2);
            for (int k = 0; k < 2; k++) {
                Location loc3 = loc2.clone().add(0, -0.5 + k, 0);
                No7A3Info.Particles.TICK_DECO.play(loc3, LocationUtil.getDirection(loc3, loc).normalize());
            }
        }
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
