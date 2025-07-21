package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.skill.HasBonusScore;
import com.dace.dmgr.combat.ability.skill.UltimateSkill;
import com.dace.dmgr.combat.ability.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;

public final class QuakerUlt extends UltimateSkill implements HasBonusScore {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-100);
    /** 둔화 상태 효과 */
    private static final Slow SLOW = new Slow(QuakerUltInfo.SLOW);

    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 기절 상태 효과 */
    private final Stun stun;

    public QuakerUlt(@NonNull CombatUser combatUser, @NonNull QuakerUltInfo skillInfo) {
        super(combatUser, skillInfo, Timespan.MAX, QuakerUltInfo.COST);

        this.bonusScoreModule = new BonusScoreModule(this);
        this.stun = new Stun(combatUser);
    }

    @Override
    protected boolean canUse() {
        QuakerA1 skill1 = combatUser.getAbilityManager().getAbility(QuakerA1Info.getInstance());
        if (skill1.isDurationFinished()) {
            combatUser.getUser().sendAlertActionBar(skill1.getDisplayName() + " 를 활성화한 상태에서만 사용할 수 있습니다.");
            return false;
        }

        return super.canUse() && isDurationFinished();
    }

    @Override
    public void onSlot() {
        setDuration();

        combatUser.setGlobalCooldown(QuakerUltInfo.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().addModifier(MODIFIER);

        QuakerWeapon weapon = combatUser.getAbilityManager().getWeapon();
        weapon.cancel();
        weapon.setVisible(false);
        weapon.use(true);

        addActionTask(new DelayTask(this::onReady, QuakerUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);

        combatUser.getMoveModule().removeModifier(MODIFIER);
        combatUser.getAbilityManager().getWeapon().setVisible(true);
    }

    @Override
    @NonNull
    public CombatScore getBonusCombatScore() {
        return QuakerUltInfo.ASSIST_SCORE;
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        cancel();

        Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(), 0, 0.3, 0);
        Vector vector = VectorUtil.getPitchAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        HashSet<Damageable> targets = new HashSet<>();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 5; j++) {
                Vector axis2 = VectorUtil.getRotatedVector(axis, vector, 11 * (j - 2.0));
                Vector vec = VectorUtil.getRotatedVector(vector, axis2, 90 + 11 * (i - 3.5));
                new QuakerUltProjectile(targets).shot(loc, vec);
            }
        }

        QuakerUltInfo.Effects.playUseReady(loc);
        QuakerUltInfo.SHAKE.send(combatUser);
    }

    private final class QuakerUltProjectile extends Projectile<Damageable> {
        private final HashSet<Damageable> targets;

        private QuakerUltProjectile(@NonNull HashSet<Damageable> targets) {
            super(QuakerUlt.this, QuakerUltInfo.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().size(QuakerUltInfo.SIZE).maxDistance(QuakerUltInfo.DISTANCE).build());
            this.targets = targets;
        }

        @Override
        protected boolean canBeRemoved() {
            return false;
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(15, location -> QuakerUltInfo.Effects.BULLET_TRAIL.apply(getVelocity()).play(location));
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (targets.add(target)) {
                    if (target.getDamageModule().damage(this, QuakerUltInfo.DAMAGE, DamageType.NORMAL, location, false, false)) {
                        target.getStatusEffectModule().apply(stun, QuakerUltInfo.STUN_DURATION);
                        target.getStatusEffectModule().apply(SLOW, QuakerUltInfo.SLOW_DURATION);

                        if (target instanceof Movable)
                            ((Movable) target).getKnockbackModule().knockback(LocationUtil.getDirection(combatUser.getLocation(),
                                    target.getLocation().add(0, 1, 0)), QuakerUltInfo.KNOCKBACK);

                        if (target.isGoalTarget()) {
                            combatUser.addScore(QuakerUltInfo.DAMAGE_SCORE);
                            bonusScoreModule.addTarget(target, QuakerUltInfo.SLOW_DURATION);
                        }
                    }

                    QuakerUltInfo.Effects.HIT_ENTITY.play(location);
                }

                return !(target instanceof Barrier);
            };
        }
    }
}
