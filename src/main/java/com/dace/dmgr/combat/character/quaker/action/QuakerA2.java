package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.*;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.function.LongConsumer;

public final class QuakerA2 extends ActiveSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    private static final String ASSIST_SCORE_COOLDOWN_ID = "QuakerA2AssistScoreTimeLimit";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "QuakerA2";

    public QuakerA2(@NonNull CombatUser combatUser) {
        super(combatUser, QuakerA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public long getDefaultCooldown() {
        return QuakerA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.getWeapon().setVisible(false);
        combatUser.setGlobalCooldown(-1);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -100);
        combatUser.playMeleeAttackAnimation(-10, 15, true);

        int delay = 0;
        for (int i = 0; i < 12; i++) {
            int index = i;

            if (i < 2)
                delay += 1;
            else if (i < 4)
                delay += 2;
            else if (i < 10)
                delay += 1;

            TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                Location loc = combatUser.getEntity().getEyeLocation();
                loc.setPitch(0);
                Vector vector = VectorUtil.getYawAxis(loc).multiply(-1);
                Vector axis = VectorUtil.getPitchAxis(loc);

                Vector vec = VectorUtil.getRotatedVector(vector, axis, (index < 2 ? -13 : -30 + index * 16));
                new QuakerA2Effect().shoot(loc, vec);

                if (index % 2 == 0)
                    QuakerA2Info.SOUND.USE.play(loc.add(vec));
                if (index == 11) {
                    TaskUtil.addTask(taskRunner, new IntervalTask(j -> !combatUser.getEntity().isOnGround(), () -> {
                        onCancelled();
                        onReady();
                    }, 1));
                }
            }, delay));
        }
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.resetGlobalCooldown();
        combatUser.setGlobalCooldown(QuakerA2Info.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        combatUser.getWeapon().setVisible(true);
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        Location loc = combatUser.getEntity().getLocation();
        loc.setPitch(0);

        QuakerA2Info.SOUND.USE_READY.play(loc);

        HashSet<Damageable> targets = new HashSet<>();
        Vector vector = VectorUtil.getPitchAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int i = 0; i < 7; i++) {
            Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 9 * (i - 3));
            new QuakerA2Projectile(targets).shoot(loc, vec);
        }

        TaskUtil.addTask(taskRunner, new IntervalTask((LongConsumer) i ->
                CombatUtil.addYawAndPitch(combatUser.getEntity(),
                        (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 7,
                        (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 6), 1, 5));
    }

    /**
     * 플레이어에게 처치 지원 점수를 지급한다.
     *
     * @param victim 피격자
     */
    public void applyAssistScore(@NonNull CombatUser victim) {
        if (CooldownUtil.getCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + victim) > 0)
            combatUser.addScore("처치 지원", QuakerA2Info.ASSIST_SCORE);
    }

    /**
     * 둔화 상태 효과 클래스.
     */
    private static final class QuakerA2Slow extends Slow {
        private static final QuakerA2Slow instance = new QuakerA2Slow();

        private QuakerA2Slow() {
            super(MODIFIER_ID, QuakerA2Info.SLOW);
        }
    }

    private final class QuakerA2Effect extends Hitscan {
        private QuakerA2Effect() {
            super(combatUser, HitscanOption.builder().trailInterval(6).maxDistance(QuakerWeaponInfo.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            if (getLocation().distance(combatUser.getEntity().getEyeLocation()) <= 1)
                return;

            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0, -0.3, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 12, 0.3, 0.3, 0.3,
                    200, 200, 200);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            return true;
        }

        @Override
        protected void onDestroy() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0, -0.3, 0);
            ParticleUtil.play(Particle.CRIT, loc, 30, 0.15, 0.15, 0.15, 0.05);
        }
    }

    private final class QuakerA2Projectile extends GroundProjectile {
        private final HashSet<Damageable> targets;

        private QuakerA2Projectile(HashSet<Damageable> targets) {
            super(combatUser, QuakerA2Info.VELOCITY, ProjectileOption.builder().trailInterval(10).size(QuakerA2Info.SIZE)
                    .maxDistance(QuakerA2Info.DISTANCE).condition(combatUser::isEnemy).build());
            this.targets = targets;
        }

        @Override
        protected void onTrailInterval() {
            Block floor = getLocation().clone().subtract(0, 0.5, 0).getBlock();
            CombatEffectUtil.playBlockHitEffect(getLocation(), floor, 3);
            ParticleUtil.play(Particle.CRIT, getLocation(), 20, 0.2, 0.05, 0.2, 0.25);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (targets.add(target)) {
                if (target.getDamageModule().damage(this, QuakerA2Info.DAMAGE, DamageType.NORMAL, getLocation(), false, true)) {
                    target.getStatusEffectModule().applyStatusEffect(combatUser, Stun.getInstance(), QuakerA2Info.STUN_DURATION);
                    target.getStatusEffectModule().applyStatusEffect(combatUser, QuakerA2Slow.instance, QuakerA2Info.SLOW_DURATION);

                    if (target instanceof CombatUser) {
                        combatUser.addScore("적 기절시킴", QuakerA2Info.DAMAGE_SCORE);
                        CooldownUtil.setCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + target, QuakerA2Info.SLOW_DURATION);
                    }
                }

                ParticleUtil.play(Particle.CRIT, getLocation(), 50, 0, 0, 0, 0.4);
            }

            return !(target instanceof Barrier);
        }
    }
}
