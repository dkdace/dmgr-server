package com.dace.dmgr.combat.combatant.inferno;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
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
        ActionManager actionManager = combatUser.getActionManager();
        return super.canUse(actionKey) && isDurationFinished() && actionManager.getSkill(InfernoA1Info.getInstance()).isDurationFinished()
                && actionManager.getSkill(InfernoA2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();

        ActionManager actionManager = combatUser.getActionManager();
        actionManager.getSkill(InfernoA1Info.getInstance()).setCooldown(Timespan.ZERO);

        InfernoWeapon weapon = (InfernoWeapon) actionManager.getWeapon();
        weapon.cancel();
        weapon.getReloadModule().resetRemainingAmmo();

        combatUser.setHitboxes(Hitbox.builder(2, 2, 2).offsetY(1).pitchFixed().build());

        shield = combatUser.getDamageModule().createShield(InfernoUltInfo.SHIELD);

        addActionTask(new IntervalTask(i -> {
            if (shield != null && shield.getHealth() == 0)
                return false;

            Location loc = combatUser.getLocation();

            InfernoUltInfo.Effects.playTick(i, loc);
            if (i < 24)
                InfernoUltInfo.Effects.playUseTick(i, loc);

            return true;
        }, isCancelled -> {
            if (!isCancelled)
                return;

            setDuration(Timespan.ZERO);
            InfernoUltInfo.Effects.DEATH.play(combatUser.getLocation());
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
        return combatUser.isDead();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    /**
     * 피해를 입었을 때 실행될 작업.
     *
     * @param damage   피해량
     * @param location 맞은 위치
     */
    void onDamage(double damage, @Nullable Location location) {
        if (!isDurationFinished())
            InfernoUltInfo.Effects.DAMAGE.apply(location, damage).play(CombatEffectUtil.getHitLocation(combatUser, location));
    }

    /**
     * 다른 엔티티를 죽였을 때 실행될 작업.
     *
     * @param victim            피격자
     * @param contributionScore 처치 기여도
     */
    void onKill(@NonNull Damageable victim, double contributionScore) {
        if (victim.isGoalTarget() && !isDurationFinished())
            combatUser.addScore("궁극기 보너스", InfernoUltInfo.KILL_SCORE * contributionScore);
    }
}
