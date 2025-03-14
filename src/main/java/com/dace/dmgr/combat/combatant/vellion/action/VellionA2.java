package com.dace.dmgr.combat.combatant.vellion.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class VellionA2 extends ActiveSkill implements HasBonusScore {
    /** 이동 속도 수정자 */
    private static final AbilityStatus.Modifier SPEED_MODIFIER = new AbilityStatus.Modifier(-VellionA2Info.READY_SLOW);
    /** 방어력 수정자 */
    private static final AbilityStatus.Modifier DEFENSE_MODIFIER = new AbilityStatus.Modifier(-VellionA2Info.DEFENSE_DECREMENT);
    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;

    /** 대상 위치 통과 불가 시 초기화 타임스탬프 */
    private Timestamp blockResetTimestamp = Timestamp.now();
    /** 활성화 완료 여부 */
    @Getter
    private boolean isEnabled = false;

    public VellionA2(@NonNull CombatUser combatUser) {
        super(combatUser, VellionA2Info.getInstance(), VellionA2Info.COOLDOWN, Timespan.MAX, 1);
        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", VellionA2Info.ASSIST_SCORE);
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
                && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished())
            new VellionA2Target().shot();
        else
            setDuration(Timespan.ZERO);
    }

    @Override
    public boolean isCancellable() {
        return !isEnabled && !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(Timespan.ZERO);
        isEnabled = false;
        combatUser.getMoveModule().getSpeedStatus().removeModifier(SPEED_MODIFIER);
    }

    @Override
    public boolean isAssistMode() {
        return true;
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
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().addModifier(DEFENSE_MODIFIER);
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§5§l저주받음!", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            VellionA2Info.PARTICLE.MARK.play(combatEntity.getLocation().add(0, combatEntity.getHeight() + 0.5, 0));

            if (provider instanceof CombatUser)
                ((CombatUser) provider).getUser().setGlowing(combatEntity.getEntity(), ChatColor.RED, Timespan.ofTicks(4));
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().removeModifier(DEFENSE_MODIFIER);
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§f저주가 풀림", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));
        }
    }

    private final class VellionA2Target extends Target<Damageable> {
        private VellionA2Target() {
            super(combatUser, VellionA2Info.MAX_DISTANCE, true, CombatUtil.EntityCondition.enemy(combatUser)
                    .and(combatEntity -> combatEntity.isCreature() && !combatEntity.getStatusEffectModule().has(VellionA2Mark.instance)));
        }

        @Override
        protected void onFindEntity(@NonNull Damageable target) {
            setDuration();
            combatUser.setGlobalCooldown(VellionA2Info.READY_DURATION);
            combatUser.getMoveModule().getSpeedStatus().addModifier(SPEED_MODIFIER);
            blockResetTimestamp = Timestamp.now().plus(VellionA2Info.BLOCK_RESET_DELAY);

            VellionA2Info.SOUND.USE.play(combatUser.getLocation());

            addActionTask(new IntervalTask(i -> {
                if (isDurationFinished() || isInvalid(combatUser, target))
                    return false;

                Location loc = combatUser.getArmLocation(MainHand.RIGHT);
                for (Location loc2 : LocationUtil.getLine(loc, target.getCenterLocation(), 0.7))
                    VellionA2Info.PARTICLE.USE_TICK_1.play(loc2, i / 15.0);
                Location loc2 = LocationUtil.getLocationFromOffset(loc, LocationUtil.getDirection(loc, target.getCenterLocation()),
                        0, 0, 1.5);
                playUseTickEffect(loc2, i);

                return target.canBeTargeted();
            }, isCancelled -> {
                if (isCancelled) {
                    onCancelled();
                    return;
                }

                isEnabled = true;
                combatUser.getMoveModule().getSpeedStatus().removeModifier(SPEED_MODIFIER);

                target.getStatusEffectModule().apply(VellionA2Mark.instance, combatUser, Timespan.ofTicks(10));

                VellionA2Info.SOUND.USE_READY.play(combatUser.getLocation());

                Location loc = combatUser.getArmLocation(MainHand.RIGHT);
                for (Location loc2 : LocationUtil.getLine(loc, target.getCenterLocation(), 0.4))
                    VellionA2Info.PARTICLE.USE_TICK_2.play(loc2);

                addTask(new IntervalTask(i -> {
                    if (isDurationFinished() || isInvalid(combatUser, target) || !target.getStatusEffectModule().has(VellionA2Mark.instance))
                        return false;

                    if (LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), target.getCenterLocation()))
                        blockResetTimestamp = Timestamp.now().plus(VellionA2Info.BLOCK_RESET_DELAY);

                    target.getStatusEffectModule().apply(VellionA2Mark.instance, combatUser, Timespan.ofTicks(10));
                    if (i % 10 == 0)
                        new VellionA2Area(target).emit(target.getCenterLocation());

                    if (target instanceof CombatUser)
                        bonusScoreModule.addTarget((CombatUser) target, Timespan.ofTicks(10));

                    return true;
                }, VellionA2.this::onCancelled, 1));
            }, 1, VellionA2Info.READY_DURATION.toTicks()));
        }

        /**
         * 사용 시 효과를 재생한다.
         *
         * @param location 대상 위치
         * @param i        인덱스
         */
        private void playUseTickEffect(@NonNull Location location, long i) {
            Vector vector = VectorUtil.getYawAxis(location);
            Vector axis = VectorUtil.getRollAxis(location);

            for (int j = (i >= 6 ? (int) i - 6 : 0); j < i; j++) {
                int angle = j * (j > 5 ? 4 : 12);

                for (int k = 0; k < 8; k++) {
                    angle += 90;
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 4 ? angle : -angle).multiply(j * 0.2);
                    Location loc = location.clone().add(vec);

                    if (i != 15)
                        VellionA2Info.PARTICLE.USE_TICK_1.play(loc, i / 15.0);
                    else
                        VellionA2Info.PARTICLE.USE_TICK_2.play(loc);
                }
            }
        }

        /**
         * 저주 효과를 유지할 수 없는지 확인한다.
         *
         * @param combatUser 플레이어
         * @param target     사용 대상
         * @return 유지할 수 없으면 {@code true} 반환
         */
        private boolean isInvalid(@NonNull CombatUser combatUser, @NonNull CombatEntity target) {
            return target.isRemoved() || blockResetTimestamp.isBefore(Timestamp.now())
                    || combatUser.getEntity().getEyeLocation().distance(target.getCenterLocation()) > VellionA2Info.MAX_DISTANCE;
        }

        private final class VellionA2Area extends Area<Damageable> {
            private final Location effectLoc;

            private VellionA2Area(@NonNull Damageable target) {
                super(combatUser, VellionA2Info.RADIUS, CombatUtil.EntityCondition.enemy(combatUser).exclude(target));

                this.effectLoc = target.getLocation().add(0, target.getHeight() + 0.5, 0);
                VellionA2Info.SOUND.TRIGGER.play(effectLoc);
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                target.getDamageModule().damage(combatUser, VellionA2Info.DAMAGE_PER_SECOND * 10 / 20.0, DamageType.NORMAL, null,
                        false, true);

                for (Location loc2 : LocationUtil.getLine(effectLoc, location, 0.4))
                    VellionA2Info.PARTICLE.HIT_ENTITY_MARK_DECO.play(loc2);
                VellionA2Info.PARTICLE.HIT_ENTITY_MARK_CORE.play(location);

                return !(target instanceof Barrier);
            }
        }
    }
}
