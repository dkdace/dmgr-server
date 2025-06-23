package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarDisplay;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public final class SiliaUlt extends UltimateSkill {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(SiliaUltInfo.SPEED);
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public SiliaUlt(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaUltInfo.getInstance(), SiliaUltInfo.DURATION, SiliaUltInfo.COST);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished() || !isEnabled)
            return null;

        return ActionBarDisplay.builder(this).title().durationBar().build();
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration(Timespan.MAX);
        combatUser.setGlobalCooldown(SiliaUltInfo.READY_DURATION);

        ActionManager actionManager = combatUser.getActionManager();
        SiliaWeapon weapon = (SiliaWeapon) actionManager.getWeapon();
        weapon.setVisible(false);

        actionManager.getSkill(SiliaA3Info.getInstance()).cancel();

        Location loc = combatUser.getLocation();

        addActionTask(new IntervalTask(i -> SiliaUltInfo.Effects.playUseTick(i, combatUser.getLocation(), loc), () -> {
            cancel();

            isEnabled = true;

            setDuration();
            combatUser.getMoveModule().addModifier(MODIFIER);

            actionManager.getSkill(SiliaA1Info.getInstance()).setCooldown(Timespan.ZERO);
            actionManager.getTrait(SiliaT2Info.getInstance()).setStrike(true);
            weapon.setVisible(true);

            SiliaUltInfo.Effects.USE_READY.play(combatUser.getLocation());
        }, 1, SiliaUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        isEnabled = false;

        combatUser.getMoveModule().removeModifier(MODIFIER);
        combatUser.getActionManager().getTrait(SiliaT2Info.getInstance()).setStrike(false);
    }

    @Override
    public boolean isCancellable() {
        return (!isEnabled || combatUser.isDead()) && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getActionManager().getWeapon().setVisible(true);
    }

    /**
     * 다른 엔티티를 죽였을 때 실행될 작업.
     *
     * @param victim            피격자
     * @param contributionScore 처치 기여도
     */
    void onKill(@NonNull Damageable victim, double contributionScore) {
        if (!victim.isGoalTarget() || isDurationFinished())
            return;

        addDuration(SiliaUltInfo.DURATION_ADD_ON_KILL);
        combatUser.addScore("궁극기 보너스", SiliaUltInfo.KILL_SCORE * contributionScore);
    }
}
