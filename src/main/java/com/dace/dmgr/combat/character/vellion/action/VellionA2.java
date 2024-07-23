package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Living;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.temporal.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public final class VellionA2 extends ActiveSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    public static final String ASSIST_SCORE_COOLDOWN_ID = "VellionA2AssistScoreTimeLimit";
    /** 대상 위치 통과 불가 시 초기화 딜레이 쿨타임 ID */
    private static final String BLOCK_RESET_DELAY_COOLDOWN_ID = "BlockResetDelay";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "VellionA2";
    private final VellionA2Mark vellionA2Mark = new VellionA2Mark();
    /** 활성화 완료 여부 */
    @Getter
    private boolean isEnabled = false;

    VellionA2(@NonNull CombatUser combatUser) {
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
    public boolean canUse() {
        return super.canUse() && !((VellionA3) combatUser.getSkill(VellionA3Info.getInstance())).getConfirmModule().isChecking();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished())
            new VellionTarget().shoot();
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
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param location 대상 위치
     * @param i        인덱스
     */
    private void playUseTickEffect(Location location, long i) {
        Vector vector = VectorUtil.getYawAxis(location);
        Vector axis = VectorUtil.getRollAxis(location);

        for (int j = (i >= 6 ? (int) i - 6 : 0); j < i; j++) {
            int angle = j * (j > 5 ? 4 : 12);

            for (int k = 0; k < 8; k++) {
                angle += 90;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 4 ? angle : -angle).multiply(j * 0.2);
                Location loc = location.clone().add(vec);

                if (i == 15)
                    ParticleUtil.play(Particle.SPELL_WITCH, loc, 1, 0, 0, 0, 0);
                else
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 1, 0, 0, 0,
                            (int) (200 - i * 4), 130, (int) (230 - i * 5));
            }
        }
    }

    /**
     * 저주 표식 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class VellionA2Mark implements StatusEffect {
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
        public void onStart(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§5§l저주받음!", "", 0, 5, 10);
            if (combatEntity instanceof Damageable)
                ((Damageable) combatEntity).getDamageModule().getDefenseMultiplierStatus().addModifier(MODIFIER_ID, -VellionA2Info.DEFENSE_DECREMENT);
        }

        @Override
        public void onTick(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider, long i) {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, combatEntity.getEntity().getLocation().add(0, combatEntity.getEntity().getHeight() + 0.5, 0),
                    4, 0.2, 0.2, 0.2, 160, 150, 152);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, combatEntity.getEntity().getLocation().add(0, combatEntity.getEntity().getHeight() + 0.5, 0),
                    1, 0, 0, 0, 160, 150, 152);
        }

        @Override
        public void onEnd(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Damageable)
                ((Damageable) combatEntity).getDamageModule().getDefenseMultiplierStatus().removeModifier(MODIFIER_ID);
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§f저주가 풀림", "", 0, 5, 10);
        }
    }

    private final class VellionTarget extends Hitscan {
        private Damageable target = null;

        private VellionTarget() {
            super(combatUser, HitscanOption.builder().size(HitscanOption.TARGET_SIZE_DEFAULT).maxDistance(VellionA2Info.MAX_DISTANCE)
                    .condition(combatEntity -> combatEntity instanceof Living && combatEntity.isEnemy(VellionA2.this.combatUser) &&
                            LocationUtil.canPass(VellionA2.this.combatUser.getEntity().getEyeLocation(), combatEntity.getCenterLocation()) &&
                            !combatEntity.getStatusEffectModule().hasStatusEffect(vellionA2Mark)).build());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            setDuration();
            combatUser.setGlobalCooldown((int) VellionA2Info.READY_DURATION);
            combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -VellionA2Info.READY_SLOW);

            this.target = target;

            SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_A2_USE, combatUser.getEntity().getLocation());

            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                if (isDurationFinished())
                    return false;

                if (LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), target.getCenterLocation()))
                    CooldownUtil.setCooldown(combatUser, BLOCK_RESET_DELAY_COOLDOWN_ID, 3);

                Location loc = combatUser.getArmLocation(true);
                for (Location loc2 : LocationUtil.getLine(loc, target.getCenterLocation(), 0.7))
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc2, 1, 0, 0, 0,
                            (int) (200 - i * 4), 130, (int) (230 - i * 5));
                Location loc2 = LocationUtil.getLocationFromOffset(loc, LocationUtil.getDirection(loc, target.getCenterLocation()),
                        0, 0, 1.5);
                playUseTickEffect(loc2, i);

                return canKeep(combatUser, target) && target.canBeTargeted();
            }, isCancelled -> {
                if (isCancelled) {
                    onCancelled();
                    return;
                }

                combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);

                SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_A2_USE_READY, combatUser.getEntity().getLocation());

                Location loc = combatUser.getArmLocation(true);
                for (Location loc2 : LocationUtil.getLine(loc, target.getCenterLocation(), 0.4))
                    ParticleUtil.play(Particle.SPELL_WITCH, loc2, 1, 0, 0, 0, 0);

                TaskUtil.addTask(VellionA2.this, new IntervalTask(i -> {
                    if (isDurationFinished())
                        return false;

                    isEnabled = true;
                    onTick(target, i);

                    return canKeep(combatUser, target);
                }, isCancelled2 -> {
                    isEnabled = false;
                    onCancelled();
                }, 1));
            }, 1, VellionA2Info.READY_DURATION));

            return false;
        }

        private void onTick(Damageable target, long i) {
            target.getStatusEffectModule().applyStatusEffect(combatUser, vellionA2Mark, 4);
            if (target instanceof CombatUser)
                CooldownUtil.setCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + target, 10);

            if (LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), target.getCenterLocation()))
                CooldownUtil.setCooldown(combatUser, BLOCK_RESET_DELAY_COOLDOWN_ID, VellionA2Info.BLOCK_RESET_DELAY);

            GlowUtil.setGlowing(target.getEntity(), ChatColor.RED, combatUser.getEntity(), 4);

            if (i % 10 == 0) {
                Location loc2 = target.getEntity().getLocation().add(0, target.getEntity().getHeight() + 0.5, 0);
                Predicate<CombatEntity> condition = combatEntity -> combatEntity.isEnemy(combatUser) && combatEntity != target &&
                        combatEntity instanceof Damageable;
                CombatEntity[] areaTargets = CombatUtil.getNearCombatEntities(combatUser.getGame(), loc2, VellionA2Info.RADIUS, condition);
                new VellionA2Area(condition, areaTargets).emit(loc2);
            }
        }

        /**
         * 저주 효과를 유지할 수 있는지 확인한다.
         *
         * @param combatUser 플레이어
         * @param target     사용 대상
         */
        private boolean canKeep(@NonNull CombatUser combatUser, @NonNull CombatEntity target) {
            return target.isEnemy(combatUser) && !target.isDisposed() &&
                    combatUser.getEntity().getEyeLocation().distance(target.getCenterLocation()) <= VellionA2Info.MAX_DISTANCE &&
                    CooldownUtil.getCooldown(combatUser, BLOCK_RESET_DELAY_COOLDOWN_ID) > 0;
        }

        @Override
        protected void onDestroy() {
            if (target == null)
                combatUser.getUser().sendAlert("대상을 찾을 수 없습니다.");
        }

        private final class VellionA2Area extends Area {
            private boolean isActivated = false;

            private VellionA2Area(Predicate<CombatEntity> condition, CombatEntity[] targets) {
                super(combatUser, VellionA2Info.RADIUS, condition, targets);
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                target.getDamageModule().damage(combatUser, VellionA2Info.DAMAGE_PER_SECOND * 10 / 20, DamageType.NORMAL, null,
                        false, true);

                for (Location loc : LocationUtil.getLine(center, location, 0.4))
                    ParticleUtil.play(Particle.SMOKE_NORMAL, loc, 1, 0, 0, 0, 0);
                ParticleUtil.play(Particle.CRIT_MAGIC, location, 15, 0, 0, 0, 0.3);
                if (!isActivated) {
                    isActivated = true;
                    SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_A2_TRIGGER, center);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
