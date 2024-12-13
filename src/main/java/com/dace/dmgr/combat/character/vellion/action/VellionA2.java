package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

@Getter
public final class VellionA2 extends ActiveSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    private static final String ASSIST_SCORE_COOLDOWN_ID = "VellionA2AssistScoreTimeLimit";
    /** 대상 위치 통과 불가 시 초기화 딜레이 쿨타임 ID */
    private static final String BLOCK_RESET_DELAY_COOLDOWN_ID = "BlockResetDelay";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "VellionA2";

    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public VellionA2(@NonNull CombatUser combatUser) {
        super(combatUser, VellionA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public long getDefaultCooldown() {
        return VellionA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && !combatUser.getSkill(VellionA3Info.getInstance()).getConfirmModule().isChecking()
                && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished())
            new VellionA2Target().shoot();
        else
            setDuration(0);
    }

    @Override
    public boolean isCancellable() {
        return !isEnabled && !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        isEnabled = false;
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
    }

    /**
     * 플레이어에게 처치 지원 점수를 지급한다.
     *
     * @param victim 피격자
     */
    public void applyAssistScore(@NonNull CombatUser victim) {
        if (CooldownUtil.getCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + victim) > 0)
            combatUser.addScore("처치 지원", VellionA2Info.ASSIST_SCORE);
    }

    /**
     * 저주 표식 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class VellionA2Mark implements StatusEffect {
        private static final VellionA2Mark instance = new VellionA2Mark();

        @Override
        @NonNull
        public StatusEffectType getStatusEffectType() {
            return StatusEffectType.NONE;
        }

        @Override
        public boolean isPositive() {
            return false;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().addModifier(MODIFIER_ID, -VellionA2Info.DEFENSE_DECREMENT);
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§5§l저주받음!", "", 0, 5, 10);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            VellionA2Info.PARTICLE.MARK.play(combatEntity.getEntity().getLocation().add(0, combatEntity.getEntity().getHeight() + 0.5, 0));

            if (!(provider instanceof CombatUser))
                return;

            if (LocationUtil.canPass(((CombatUser) provider).getEntity().getEyeLocation(), combatEntity.getCenterLocation()))
                CooldownUtil.setCooldown(provider, BLOCK_RESET_DELAY_COOLDOWN_ID, VellionA2Info.BLOCK_RESET_DELAY);

            ((CombatUser) provider).getUser().setGlowing(combatEntity.getEntity(), ChatColor.RED, 4);

            if (combatEntity instanceof CombatUser)
                CooldownUtil.setCooldown(provider, ASSIST_SCORE_COOLDOWN_ID + combatEntity, 10);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().removeModifier(MODIFIER_ID);
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§f저주가 풀림", "", 0, 5, 10);
        }
    }

    private final class VellionA2Target extends Target {
        private VellionA2Target() {
            super(combatUser, VellionA2Info.MAX_DISTANCE, true, combatEntity -> ((Damageable) combatEntity).getDamageModule().isLiving()
                    && combatEntity.isEnemy(VellionA2.this.combatUser)
                    && !((Damageable) combatEntity).getStatusEffectModule().hasStatusEffect(VellionA2Mark.instance));
        }

        @Override
        protected void onFindEntity(@NonNull Damageable target) {
            setDuration();
            combatUser.setGlobalCooldown((int) VellionA2Info.READY_DURATION);
            combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -VellionA2Info.READY_SLOW);
            CooldownUtil.setCooldown(combatUser, BLOCK_RESET_DELAY_COOLDOWN_ID, VellionA2Info.BLOCK_RESET_DELAY);

            VellionA2Info.SOUND.USE.play(combatUser.getEntity().getLocation());

            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                if (isDurationFinished() || isInvalid(combatUser, target))
                    return false;

                Location loc = combatUser.getArmLocation(true);
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
                combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);

                target.getStatusEffectModule().applyStatusEffect(combatUser, VellionA2Mark.instance, 10);

                VellionA2Info.SOUND.USE_READY.play(combatUser.getEntity().getLocation());

                Location loc = combatUser.getArmLocation(true);
                for (Location loc2 : LocationUtil.getLine(loc, target.getCenterLocation(), 0.4))
                    VellionA2Info.PARTICLE.USE_TICK_2.play(loc2);

                TaskUtil.addTask(VellionA2.this, new IntervalTask(i -> {
                    if (isDurationFinished() || isInvalid(combatUser, target) || !target.getStatusEffectModule().hasStatusEffect(VellionA2Mark.instance))
                        return false;

                    target.getStatusEffectModule().applyStatusEffect(combatUser, VellionA2Mark.instance, 10);
                    if (i % 10 == 0)
                        new VellionA2Area(target).emit(target.getCenterLocation());

                    return true;
                }, VellionA2.this::onCancelled, 1));
            }, 1, VellionA2Info.READY_DURATION));
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
            return target.isDisposed() || CooldownUtil.getCooldown(combatUser, BLOCK_RESET_DELAY_COOLDOWN_ID) == 0
                    || combatUser.getEntity().getEyeLocation().distance(target.getCenterLocation()) > VellionA2Info.MAX_DISTANCE;
        }

        private final class VellionA2Area extends Area {
            private final Location effectLoc;

            private VellionA2Area(Damageable target) {
                super(combatUser, VellionA2Info.RADIUS, combatEntity -> combatEntity instanceof Damageable && combatEntity != target
                        && combatEntity.isEnemy(VellionA2.this.combatUser));

                effectLoc = target.getEntity().getLocation().add(0, target.getEntity().getHeight() + 0.5, 0);
                VellionA2Info.SOUND.TRIGGER.play(effectLoc);
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
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
