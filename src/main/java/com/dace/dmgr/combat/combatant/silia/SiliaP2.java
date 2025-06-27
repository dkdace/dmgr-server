package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Set;

public final class SiliaP2 extends PassiveSkill {
    /** 벽타기 남은 횟수 */
    private int wallRideCount = SiliaP2Info.USE_COUNT;

    public SiliaP2(@NonNull CombatUser combatUser, @NonNull SiliaP2Info skillInfo) {
        super(combatUser, skillInfo, Timespan.ZERO, Timespan.MAX);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.LEFT_CLICK);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && canActivate();
    }

    /**
     * 스킬 활성화 조건을 확인한다.
     *
     * @return 활성화 조건
     */
    private boolean canActivate() {
        if (wallRideCount <= 0 || !LocationUtil.isNonSolid(combatUser.getEntity().getEyeLocation().add(0, 0.5, 0)))
            return false;

        Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.1, 0);
        loc.setPitch(0);
        loc.add(loc.getDirection().multiply(0.75));

        return !LocationUtil.isNonSolid(loc);
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.addYawAndPitch(0, 0);

        AbilityManager abilityManager = combatUser.getAbilityManager();
        abilityManager.getWeapon().setVisible(false);

        double distance = combatUser.getEntity().getEyeLocation().distance(combatUser.getEntity().getTargetBlock(null, 1).getLocation());
        if (distance < 1)
            combatUser.getMoveModule().teleport(LocationUtil.getLocationFromOffset(combatUser.getLocation(), 0, 0, -1 + distance));

        addActionTask(new IntervalTask(i -> {
            if (combatUser.getKnockbackModule().isKnockbacked() || !canActivate())
                return false;

            combatUser.getMoveModule().push(new Vector(0, SiliaP2Info.PUSH, 0), true);
            combatUser.getEntity().setFallDistance(0);
            combatUser.getUser().sendTitle("", StringFormUtil.getProgressBar(--wallRideCount, 10, ChatColor.WHITE), Timespan.ZERO,
                    Timespan.ofTicks(10), Timespan.ofTicks(5));

            if (abilityManager.getAbility(SiliaA3Info.getInstance()).isDurationFinished())
                SiliaP2Info.Effects.USE.play(combatUser.getLocation());
            else
                SiliaP2Info.Effects.USE_A3.play(combatUser.getLocation());

            return true;
        }, isCancelled -> {
            cancel();

            wallRideCount--;

            Location loc = combatUser.getLocation();
            loc.setPitch(-65);
            combatUser.getMoveModule().push(loc.getDirection().multiply(SiliaP2Info.PUSH), true);
        }, 3, 10));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getAbilityManager().getWeapon().setVisible(true);

        addTask(new IntervalTask(i -> !combatUser.getEntity().isOnGround(), () -> wallRideCount = SiliaP2Info.USE_COUNT, 1));
    }
}
