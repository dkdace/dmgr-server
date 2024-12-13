package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.*;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.function.LongConsumer;

public final class QuakerUlt extends UltimateSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    private static final String ASSIST_SCORE_COOLDOWN_ID = "QuakerUltAssistScoreTimeLimit";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "QuakerUlt";

    public QuakerUlt(@NonNull CombatUser combatUser) {
        super(combatUser, QuakerUltInfo.getInstance());
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public int getCost() {
        return QuakerUltInfo.COST;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        if (combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished()) {
            combatUser.getUser().sendAlert(QuakerA1Info.getInstance() + " 를 활성화한 상태에서만 사용할 수 있습니다.");
            return false;
        }

        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.getWeapon().setVisible(false);
        combatUser.setGlobalCooldown(QuakerUltInfo.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -100);
        combatUser.playMeleeAttackAnimation(-10, 16, true);

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            int delay = 0;
            for (int i = 0; i < 8; i++) {
                int index = i;

                if (i == 1)
                    delay += 2;
                else if (i == 2 || i == 4 || i == 6 || i == 7)
                    delay += 1;

                TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                    Location loc = combatUser.getEntity().getEyeLocation();
                    Vector vector = VectorUtil.getPitchAxis(loc);
                    Vector axis = VectorUtil.getYawAxis(loc);

                    Vector vec = VectorUtil.getRotatedVector(vector, axis, (index + 1) * 20);
                    new QuakerUltEffect().shoot(loc, vec);

                    CombatUtil.addYawAndPitch(combatUser.getEntity(), 0.8, 0.1);
                    if (index % 2 == 0)
                        QuakerWeaponInfo.SOUND.USE.play(loc.add(vec));
                    if (index == 7) {
                        CombatUtil.addYawAndPitch(combatUser.getEntity(), -1, -0.7);
                        onCancelled();
                        onReady();
                    }
                }, delay));
            }
        }, 2));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        combatUser.getWeapon().setVisible(true);
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(), 0, 0.3, 0);

        QuakerUltInfo.SOUND.USE_READY.play(loc);
        QuakerUltInfo.PARTICLE.USE_READY.play(LocationUtil.getLocationFromOffset(loc, 0, 0, 1.5));

        HashSet<Damageable> targets = new HashSet<>();
        Vector vector = VectorUtil.getPitchAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 5; j++) {
                Vector axis2 = VectorUtil.getRotatedVector(axis, vector, 11 * (j - 2.0));
                Vector vector2 = VectorUtil.getRotatedVector(vector, vector, 11 * (j - 2.0));
                Vector vec = VectorUtil.getRotatedVector(vector2, axis2, 90 + 11 * (i - 3.5));
                new QuakerUltProjectile(targets).shoot(loc, vec);
            }
        }

        TaskUtil.addTask(taskRunner, new IntervalTask((LongConsumer) i ->
                CombatUtil.addYawAndPitch(combatUser.getEntity(),
                        (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 10,
                        (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 8), 1, 6));
    }

    /**
     * 플레이어에게 처치 지원 점수를 지급한다.
     *
     * @param victim 피격자
     */
    public void applyAssistScore(@NonNull CombatUser victim) {
        if (CooldownUtil.getCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + victim) > 0)
            combatUser.addScore("처치 지원", QuakerUltInfo.ASSIST_SCORE);
    }

    /**
     * 둔화 상태 효과 클래스.
     */
    private static final class QuakerUltSlow extends Slow {
        private static final QuakerUltSlow instance = new QuakerUltSlow();

        private QuakerUltSlow() {
            super(MODIFIER_ID, QuakerUltInfo.SLOW);
        }
    }

    private final class QuakerUltEffect extends Hitscan {
        private QuakerUltEffect() {
            super(combatUser, HitscanOption.builder().trailInterval(6).maxDistance(QuakerWeaponInfo.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            if (getLocation().distance(combatUser.getEntity().getEyeLocation()) <= 1)
                return;

            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0, -0.3, 0);
            QuakerWeaponInfo.PARTICLE.BULLET_TRAIL_CORE.play(loc);
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
            QuakerWeaponInfo.PARTICLE.BULLET_TRAIL_DECO.play(loc);
        }
    }

    private final class QuakerUltProjectile extends Projectile {
        private final HashSet<Damageable> targets;

        private QuakerUltProjectile(HashSet<Damageable> targets) {
            super(combatUser, QuakerUltInfo.VELOCITY, ProjectileOption.builder().trailInterval(15).size(QuakerUltInfo.SIZE)
                    .maxDistance(QuakerUltInfo.DISTANCE).condition(combatUser::isEnemy).build());
            this.targets = targets;
        }

        @Override
        protected void onTrailInterval() {
            Vector vec = VectorUtil.getSpreadedVector(getVelocity().clone().normalize(), 20);
            QuakerUltInfo.PARTICLE.BULLET_TRAIL.play(getLocation(), vec);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (targets.add(target)) {
                if (target.getDamageModule().damage(this, QuakerUltInfo.DAMAGE, DamageType.NORMAL, getLocation(), false, false)) {
                    target.getStatusEffectModule().applyStatusEffect(combatUser, Stun.getInstance(), QuakerUltInfo.STUN_DURATION);
                    target.getStatusEffectModule().applyStatusEffect(combatUser, QuakerUltSlow.instance, QuakerUltInfo.SLOW_DURATION);
                    target.getKnockbackModule().knockback(LocationUtil.getDirection(combatUser.getEntity().getLocation(),
                            target.getEntity().getLocation().add(0, 1, 0)).multiply(QuakerUltInfo.KNOCKBACK));

                    if (target instanceof CombatUser) {
                        combatUser.addScore("적 기절시킴", QuakerUltInfo.DAMAGE_SCORE);
                        CooldownUtil.setCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + target, QuakerUltInfo.SLOW_DURATION);
                    }
                }

                QuakerUltInfo.PARTICLE.HIT_ENTITY.play(getLocation());
            }

            return !(target instanceof Barrier);
        }
    }
}
