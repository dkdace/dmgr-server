package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;

public final class QuakerUlt extends UltimateSkill implements HasBonusScore {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-100);
    /** 둔화 상태 효과 */
    private static final Slow SLOW = new Slow(QuakerUltInfo.SLOW);

    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 기절 상태 효과 */
    private final Stun stun;

    public QuakerUlt(@NonNull CombatUser combatUser) {
        super(combatUser, QuakerUltInfo.getInstance(), Timespan.MAX, QuakerUltInfo.COST);

        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", QuakerUltInfo.ASSIST_SCORE);
        this.stun = new Stun(combatUser);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        QuakerA1 skill1 = combatUser.getSkill(QuakerA1Info.getInstance());
        if (skill1.isDurationFinished()) {
            combatUser.getUser().sendAlertActionBar(skill1.getSkillInfo() + " 를 활성화한 상태에서만 사용할 수 있습니다.");
            return false;
        }

        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();

        combatUser.setGlobalCooldown(QuakerUltInfo.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        QuakerWeapon weapon = (QuakerWeapon) combatUser.getWeapon();
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

        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
        combatUser.getWeapon().setVisible(true);
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

        QuakerUltInfo.SOUND.USE_READY.play(loc);
        QuakerUltInfo.PARTICLE.USE_READY.play(LocationUtil.getLocationFromOffset(loc, 0, 0, 1.5));
        CombatUtil.sendShake(combatUser, 10, 8, Timespan.ofTicks(6));
    }

    private final class QuakerUltProjectile extends Projectile<Damageable> {
        private final HashSet<Damageable> targets;

        private QuakerUltProjectile(@NonNull HashSet<Damageable> targets) {
            super(QuakerUlt.this, QuakerUltInfo.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
                    Option.builder().size(QuakerUltInfo.SIZE).maxDistance(QuakerUltInfo.DISTANCE).build());
            this.targets = targets;
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(15, location -> {
                Vector vec = VectorUtil.getSpreadedVector(getVelocity().normalize(), 20);
                QuakerUltInfo.PARTICLE.BULLET_TRAIL.play(location, vec);
            });
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

                        if (target instanceof Movable) {
                            Vector dir = LocationUtil.getDirection(combatUser.getLocation(), target.getLocation().add(0, 1, 0))
                                    .multiply(QuakerUltInfo.KNOCKBACK);
                            ((Movable) target).getMoveModule().knockback(dir);
                        }

                        if (target.isGoalTarget()) {
                            combatUser.addScore("적 기절시킴", QuakerUltInfo.DAMAGE_SCORE);
                            bonusScoreModule.addTarget(target, QuakerUltInfo.SLOW_DURATION);
                        }
                    }

                    QuakerUltInfo.PARTICLE.HIT_ENTITY.play(location);
                }

                return !(target instanceof Barrier);
            };
        }
    }
}
