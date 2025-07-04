package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.skill.HasBonusScore;
import com.dace.dmgr.combat.ability.skill.UltimateSkill;
import com.dace.dmgr.combat.ability.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.Grounding;
import com.dace.dmgr.combat.entity.module.statuseffect.Invulnerable;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public final class VellionUlt extends UltimateSkill implements HasBonusScore {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-100);
    /** 둔화 상태 효과 */
    private static final Slow SLOW = new Slow(VellionUltInfo.SLOW);

    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 기절 상태 효과 */
    private final Stun stun;
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public VellionUlt(@NonNull CombatUser combatUser, @NonNull VellionUltInfo skillInfo) {
        super(combatUser, skillInfo, VellionUltInfo.DURATION, VellionUltInfo.COST);

        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", VellionUltInfo.ASSIST_SCORE);
        this.stun = new Stun(combatUser);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished() || !isEnabled)
            return null;

        return ActionBarDisplay.builder(this).title().durationBar().build();
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished()
                && !combatUser.getAbilityManager().getAbility(VellionA3Info.getInstance()).getConfirmModule().isChecking();
    }

    @Override
    public void onSlot() {
        setDuration(Timespan.MAX);

        combatUser.setGlobalCooldown(VellionUltInfo.READY_DURATION);
        combatUser.getMoveModule().addModifier(MODIFIER);

        combatUser.getAbilityManager().getAbility(VellionP1Info.getInstance()).cancel();

        VellionUltInfo.Effects.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> VellionUltInfo.Effects.playUseTick(i, combatUser.getLocation()), () ->
                addActionTask(new IntervalTask(i -> !combatUser.getEntity().isOnGround(), this::onReady, 1)),
                1, VellionUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        isEnabled = false;

        combatUser.getMoveModule().removeModifier(MODIFIER);
    }

    @Override
    public boolean isCancellable() {
        return (!isEnabled || combatUser.isDead()) && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        isEnabled = true;

        setDuration();
        combatUser.getStatusEffectModule().apply(Invulnerable.getInstance(), VellionUltInfo.DURATION);

        VellionUltInfo.Effects.USE_READY.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getEntity().getEyeLocation();
            if (i % 4 == 0)
                new VellionUltArea().emit(loc);

            VellionUltInfo.Effects.playTick(i, combatUser.getLocation(), loc);
        }, () -> {
            forceCancel();

            Location loc = combatUser.getEntity().getEyeLocation();
            new VellionUltExplodeArea().emit(loc);

            VellionUltInfo.Effects.EXPLODE.play(loc.add(0, 1, 0));
        }, 1, VellionUltInfo.DURATION.toTicks()));
    }

    private final class VellionUltArea extends Area<Damageable> {
        private VellionUltArea() {
            super(combatUser, VellionUltInfo.RADIUS, EntityCondition.enemy(combatUser).and(Damageable::isCreature));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null, false, false)) {
                target.getStatusEffectModule().apply(SLOW, Timespan.ofTicks(10));
                target.getStatusEffectModule().apply(Grounding.getInstance(), Timespan.ofTicks(10));

                if (target.isGoalTarget())
                    bonusScoreModule.addTarget(target, Timespan.ofTicks(10));
            }

            return true;
        }
    }

    private final class VellionUltExplodeArea extends Area<Damageable> {
        private VellionUltExplodeArea() {
            super(combatUser, VellionUltInfo.RADIUS, EntityCondition.enemy(combatUser).and(Damageable::isCreature));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, target.getDamageModule().getMaxHealth() * VellionUltInfo.DAMAGE_RATIO,
                    DamageType.FIXED, null, false, false)) {
                target.getStatusEffectModule().apply(stun, VellionUltInfo.STUN_DURATION);

                if (target.isGoalTarget()) {
                    combatUser.addScore("결계 발동", VellionUltInfo.DAMAGE_SCORE);
                    bonusScoreModule.addTarget(target, VellionUltInfo.STUN_DURATION);
                }
            }

            VellionUltInfo.Effects.playHitEntity(location, combatUser.getEntity().getEyeLocation(), target.getCenterLocation());

            return true;
        }
    }
}
