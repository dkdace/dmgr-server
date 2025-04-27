package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.Targeted;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.action.skill.module.TargetModule;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class VellionA2 extends ActiveSkill implements Targeted<Damageable>, HasBonusScore {
    /** 이동 속도 수정자 */
    private static final AbilityStatus.Modifier SPEED_MODIFIER = new AbilityStatus.Modifier(-VellionA2Info.READY_SLOW);
    /** 방어력 수정자 */
    private static final AbilityStatus.Modifier DEFENSE_MODIFIER = new AbilityStatus.Modifier(-VellionA2Info.DEFENSE_DECREMENT);

    /** 타겟 모듈 */
    @NonNull
    @Getter
    private final TargetModule<Damageable> targetModule;
    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 대상 위치 통과 불가 시 초기화 타임스탬프 */
    private Timestamp blockResetTimestamp = Timestamp.now();
    /** 활성화 완료 여부 */
    @Getter(AccessLevel.PACKAGE)
    private boolean isEnabled = false;

    public VellionA2(@NonNull CombatUser combatUser) {
        super(combatUser, VellionA2Info.getInstance(), VellionA2Info.COOLDOWN, Timespan.MAX, 1);

        this.targetModule = new TargetModule<>(this, VellionA2Info.MAX_DISTANCE);
        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", VellionA2Info.ASSIST_SCORE);

        addOnReset(this::forceCancel);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        if (isDurationFinished() || !isEnabled)
            return null;

        return VellionA2Info.getInstance() + ActionBarStringUtil.getKeyInfo(this, "해제");
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && !combatUser.getSkill(VellionA3Info.getInstance()).getConfirmModule().isChecking()
                && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished() && (!isDurationFinished() || targetModule.findTarget());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (!isDurationFinished()) {
            forceCancel();
            return;
        }

        setDuration();

        combatUser.setGlobalCooldown(VellionA2Info.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(SPEED_MODIFIER);

        VellionA2Info.Sounds.USE.play(combatUser.getLocation());

        Damageable target = targetModule.getCurrentTarget();

        addActionTask(new IntervalTask(i -> {
            if (!target.canBeTargeted() || isInvalid(target))
                return false;

            for (Location loc : LocationUtil.getLine(combatUser.getArmLocation(MainHand.RIGHT), target.getCenterLocation(), 0.7))
                VellionA2Info.Particles.USE_TICK_1.play(loc, i / 15.0);

            playUseTickEffect(target, i);

            return true;
        }, isCancelled -> {
            if (isCancelled)
                cancel();
            else
                onReady(target);
        }, 1, VellionA2Info.READY_DURATION.toTicks()));
    }

    /**
     * 시전 완료 시 실행할 작업.
     *
     * @param target 대상 엔티티
     */
    private void onReady(@NonNull Damageable target) {
        isEnabled = true;

        combatUser.getMoveModule().getSpeedStatus().removeModifier(SPEED_MODIFIER);
        target.getStatusEffectModule().apply(VellionA2Mark.instance, Timespan.MAX);

        VellionA2Info.Sounds.USE_READY.play(combatUser.getLocation());

        for (Location loc : LocationUtil.getLine(combatUser.getArmLocation(MainHand.RIGHT), target.getCenterLocation(), 0.4))
            VellionA2Info.Particles.USE_TICK_2.play(loc);

        addActionTask(new IntervalTask(i -> {
            if (isInvalid(target) || !target.getStatusEffectModule().has(VellionA2Mark.instance))
                return false;

            if (LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), target.getCenterLocation()))
                blockResetTimestamp = Timestamp.now().plus(VellionA2Info.BLOCK_RESET_DELAY);
            if (blockResetTimestamp.isBefore(Timestamp.now()))
                return false;

            combatUser.setGlowing(target, Timespan.ofTicks(4));

            if (i % 10 == 0)
                new VellionA2Area(target).emit(target.getCenterLocation());

            if (target.isGoalTarget())
                bonusScoreModule.addTarget(target, Timespan.ofTicks(10));

            return true;
        }, VellionA2.this::forceCancel, 1));
    }

    @Override
    public boolean isCancellable() {
        return (!isEnabled || combatUser.isDead()) && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        if (isEnabled) {
            isEnabled = false;
            targetModule.getCurrentTarget().getStatusEffectModule().remove(VellionA2Mark.instance);
        }

        setDuration(Timespan.ZERO);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(SPEED_MODIFIER);
    }

    @Override
    @NonNull
    public EntityCondition<Damageable> getEntityCondition() {
        return EntityCondition.enemy(combatUser).and(combatEntity ->
                combatEntity.isCreature() && !combatEntity.getStatusEffectModule().has(VellionA2Mark.instance));
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param target 사용 대상
     * @param i      인덱스
     */
    private void playUseTickEffect(@NonNull Damageable target, long i) {
        Location location = combatUser.getArmLocation(MainHand.RIGHT);
        Location loc = LocationUtil.getLocationFromOffset(location, LocationUtil.getDirection(location, target.getCenterLocation()),
                0, 0, 1.5);
        Vector vector = VectorUtil.getYawAxis(loc);
        Vector axis = VectorUtil.getRollAxis(loc);

        for (long j = (i >= 6 ? i - 6 : 0); j < i; j++) {
            long angle = j * (j > 5 ? 4 : 12);

            for (int k = 0; k < 8; k++) {
                angle += 360 / 4;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 4 ? angle : -angle).multiply(j * 0.2);
                Location loc2 = loc.clone().add(vec);

                if (i != 15)
                    VellionA2Info.Particles.USE_TICK_1.play(loc2, i / 15.0);
                else
                    VellionA2Info.Particles.USE_TICK_2.play(loc2);
            }
        }
    }

    /**
     * 저주 효과를 유지할 수 없는지 확인한다.
     *
     * @param target 사용 대상
     * @return 유지할 수 없으면 {@code true} 반환
     */
    private boolean isInvalid(@NonNull CombatEntity target) {
        return target.isRemoved() || combatUser.getEntity().getEyeLocation().distance(target.getCenterLocation()) > VellionA2Info.MAX_DISTANCE;
    }

    /**
     * 저주 표식 상태 효과 클래스.
     */
    private static final class VellionA2Mark extends StatusEffect {
        private static final VellionA2Mark instance = new VellionA2Mark();

        private VellionA2Mark() {
            super(false);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().addModifier(DEFENSE_MODIFIER);
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§5§l저주받음!", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            VellionA2Info.Particles.MARK.play(combatEntity.getLocation().add(0, combatEntity.getHeight() + 0.5, 0));
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().removeModifier(DEFENSE_MODIFIER);
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§f저주가 풀림", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));
        }
    }

    private final class VellionA2Area extends Area<Damageable> {
        private final Location effectLoc;
        private boolean isActivated = false;

        private VellionA2Area(@NonNull Damageable target) {
            super(combatUser, VellionA2Info.RADIUS, EntityCondition.enemy(combatUser).exclude(target));
            this.effectLoc = target.getLocation().add(0, target.getHeight() + 0.5, 0);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            target.getDamageModule().damage(combatUser, VellionA2Info.DAMAGE_PER_SECOND * 10 / 20.0, DamageType.NORMAL, null,
                    false, true);

            if (!isActivated) {
                isActivated = true;
                VellionA2Info.Sounds.TRIGGER.play(effectLoc);
            }

            VellionA2Info.Particles.HIT_ENTITY_MARK_CORE.play(location);
            for (Location loc2 : LocationUtil.getLine(effectLoc, location, 0.4))
                VellionA2Info.Particles.HIT_ENTITY_MARK_DECO.play(loc2);

            return !(target instanceof Barrier);
        }
    }
}
