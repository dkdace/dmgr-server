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
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public final class VellionA2 extends ActiveSkill implements Targeted<Damageable>, HasBonusScore {
    /** 이동 속도 수정자 */
    private static final Modifier SPEED_MODIFIER = new Modifier(-VellionA2Info.READY_SLOW);
    /** 방어력 수정자 */
    private static final Modifier DEFENSE_MODIFIER = new Modifier(-VellionA2Info.DEFENSE_DECREMENT);

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
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.SLOT_2);
    }

    @Override
    @Nullable
    public String getActionBarString() {
        if (isDurationFinished() || !isEnabled)
            return null;

        return VellionA2Info.getInstance() + ActionBarStringUtil.getKeyInfo("해제", ActionKey.SLOT_2);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ActionManager actionManager = combatUser.getActionManager();
        return super.canUse(actionKey) && !actionManager.getSkill(VellionA3Info.getInstance()).getConfirmModule().isChecking()
                && actionManager.getSkill(VellionUltInfo.getInstance()).isDurationFinished() && (!isDurationFinished() || targetModule.findTarget());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (!isDurationFinished()) {
            forceCancel();
            return;
        }

        setDuration();

        combatUser.setGlobalCooldown(VellionA2Info.READY_DURATION);
        combatUser.getMoveModule().addModifier(SPEED_MODIFIER);

        VellionA2Info.Effects.USE.play(combatUser.getLocation());

        Damageable target = targetModule.getCurrentTarget();

        addActionTask(new IntervalTask(i -> {
            if (!target.canBeTargeted() || isInvalid(target))
                return false;

            VellionA2Info.Effects.playUseTick(i, combatUser.getArmLocation(MainHand.RIGHT), target.getCenterLocation());

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

        combatUser.getMoveModule().removeModifier(SPEED_MODIFIER);
        target.getStatusEffectModule().apply(VellionA2Mark.instance, Timespan.MAX);

        VellionA2Info.Effects.USE_READY.play(combatUser.getLocation());

        for (Location loc : LocationUtil.getLine(combatUser.getArmLocation(MainHand.RIGHT), target.getCenterLocation(), 0.4))
            VellionA2Info.Effects.USE_TICK_2.play(loc);

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
        combatUser.getMoveModule().removeModifier(SPEED_MODIFIER);
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
    private static final class VellionA2Mark implements StatusEffect {
        private static final VellionA2Mark instance = new VellionA2Mark();

        @Override
        public boolean isPositive() {
            return false;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            combatEntity.getDamageModule().addModifier(DEFENSE_MODIFIER);
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§5§l저주받음!", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            VellionA2Info.Effects.MARK.play(combatEntity.getLocation().add(0, combatEntity.getHeight() + 0.5, 0));
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            combatEntity.getDamageModule().removeModifier(DEFENSE_MODIFIER);
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
                VellionA2Info.Effects.TRIGGER.play(effectLoc);
            }

            VellionA2Info.Effects.playMarkHitEntity(effectLoc, location);

            return !(target instanceof Barrier);
        }
    }
}
