package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.entity.temporal.Barrier;
import com.dace.dmgr.combat.interaction.*;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;

public final class QuakerUlt extends UltimateSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    public static final String ASSIST_SCORE_COOLDOWN_ID = "QuakerUltAssistScoreTimeLimit";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "QuakerUlt";

    QuakerUlt(@NonNull CombatUser combatUser) {
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
    public boolean canUse() {
        if (combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished()) {
            combatUser.getUser().sendAlert(QuakerA1Info.getInstance() + " 를 활성화한 상태에서만 사용할 수 있습니다.");
            return false;
        }

        return super.canUse() && isDurationFinished();
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
                final int index = i;

                switch (i) {
                    case 1:
                        delay += 2;
                        break;
                    case 2:
                    case 4:
                    case 6:
                    case 7:
                        delay += 1;
                        break;
                }

                TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                    Location loc = combatUser.getEntity().getEyeLocation();
                    Vector vector = VectorUtil.getPitchAxis(loc);
                    Vector axis = VectorUtil.getYawAxis(loc);

                    Vector vec = VectorUtil.getRotatedVector(vector, axis, (index + 1) * 20);
                    new QuakerUltEffect().shoot(loc, vec);

                    CombatUtil.addYawAndPitch(combatUser.getEntity(), 0.8, 0.1);
                    if (index % 2 == 0)
                        SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_WEAPON_USE, loc.add(vec));
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

        SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_ULT_USE_READY, loc);
        ParticleUtil.play(Particle.CRIT, LocationUtil.getLocationFromOffset(loc, 0, 0, 1.5), 100,
                0.2, 0.2, 0.2, 0.6);

        HashSet<CombatEntity> targets = new HashSet<>();
        Vector vector = VectorUtil.getPitchAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 4; j++) {
                Vector axis2 = VectorUtil.getRotatedVector(axis, vector, 13 * (j - 1.5));
                Vector vector2 = VectorUtil.getRotatedVector(vector, vector, 13 * (j - 1.5));
                Vector vec = VectorUtil.getRotatedVector(vector2, axis2, 90 + 12 * (i - 3.5));
                new QuakerUltProjectile(targets).shoot(loc, vec);
            }
        }

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            CombatUtil.addYawAndPitch(combatUser.getEntity(), (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 10,
                    (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 8);
            return true;
        }, 1, 6));
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
        protected void trail() {
            if (location.distance(combatUser.getEntity().getEyeLocation()) <= 1)
                return;

            Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
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
            Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.play(Particle.CRIT, loc, 30, 0.15, 0.15, 0.15, 0.05);
        }
    }

    private final class QuakerUltProjectile extends Projectile {
        private final HashSet<CombatEntity> targets;

        private QuakerUltProjectile(HashSet<CombatEntity> targets) {
            super(combatUser, QuakerUltInfo.VELOCITY, ProjectileOption.builder().trailInterval(14).size(QuakerUltInfo.SIZE)
                    .maxDistance(QuakerUltInfo.DISTANCE).condition(combatUser::isEnemy).build());

            this.targets = targets;
        }

        @Override
        protected void trail() {
            Vector vec = VectorUtil.getSpreadedVector(velocity.clone().normalize(), 15);
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 0, vec.getX(), vec.getY(), vec.getZ(), 1);
            ParticleUtil.play(Particle.CRIT, location, 4, 0.2, 0.2, 0.2, 0.1);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (targets.add(target)) {
                if (target.getDamageModule().damage(this, QuakerUltInfo.DAMAGE, DamageType.NORMAL, location, false, false)) {
                    target.getStatusEffectModule().applyStatusEffect(combatUser, Stun.getInstance(), QuakerUltInfo.STUN_DURATION);
                    target.getStatusEffectModule().applyStatusEffect(combatUser, QuakerUltSlow.instance, QuakerUltInfo.SLOW_DURATION);
                    target.getKnockbackModule().knockback(LocationUtil.getDirection(combatUser.getEntity().getLocation(),
                            target.getEntity().getLocation().add(0, 1, 0)).multiply(QuakerUltInfo.KNOCKBACK), true);
                    if (target instanceof CombatUser) {
                        combatUser.addScore("적 기절시킴", QuakerUltInfo.DAMAGE_SCORE);
                        CooldownUtil.setCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + target, QuakerUltInfo.SLOW_DURATION);
                    }
                }

                ParticleUtil.play(Particle.CRIT, location, 60, 0, 0, 0, 0.4);
            }

            return !(target instanceof Barrier);
        }
    }
}
