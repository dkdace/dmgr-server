package com.dace.dmgr.combat.combatant.quaker.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.HashSet;

@Getter
public final class QuakerA2 extends ActiveSkill implements HasBonusScore {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-100);
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public QuakerA2(@NonNull CombatUser combatUser) {
        super(combatUser, QuakerA2Info.getInstance(), QuakerA2Info.COOLDOWN, Timespan.MAX, 1);
        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", QuakerA2Info.ASSIST_SCORE);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().cancel();
        combatUser.getWeapon().setVisible(false);
        combatUser.setGlobalCooldown(Timespan.MAX);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);
        combatUser.playMeleeAttackAnimation(-10, Timespan.ofTicks(15), MainHand.RIGHT);

        int delay = 0;
        for (int i = 0; i < 12; i++) {
            int index = i;

            if (i < 2)
                delay += 1;
            else if (i < 4)
                delay += 2;
            else if (i < 10)
                delay += 1;

            addActionTask(new DelayTask(() -> {
                Location loc = combatUser.getEntity().getEyeLocation();
                loc.setPitch(0);
                Vector vector = VectorUtil.getYawAxis(loc).multiply(-1);
                Vector axis = VectorUtil.getPitchAxis(loc);

                Vector vec = VectorUtil.getRotatedVector(vector, axis, (index < 2 ? -13 : -30 + index * 16));
                new QuakerA2Effect().shot(loc, vec);

                if (index % 2 == 0)
                    QuakerA2Info.SOUND.USE.play(loc.add(vec));
                if (index == 11) {
                    addActionTask(new IntervalTask(j -> !combatUser.getEntity().isOnGround(), () -> {
                        cancel();
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
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.resetGlobalCooldown();
        combatUser.setGlobalCooldown(QuakerA2Info.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
        combatUser.getWeapon().setVisible(true);
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        Location loc = combatUser.getLocation();
        loc.setPitch(0);

        QuakerA2Info.SOUND.USE_READY.play(loc);

        HashSet<Damageable> targets = new HashSet<>();
        Vector vector = VectorUtil.getPitchAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int i = 0; i < 7; i++) {
            Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 9 * (i - 3));
            new QuakerA2Projectile(targets).shot(loc, vec);
        }

        CombatUtil.sendShake(combatUser, 7, 6, Timespan.ofTicks(5));
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    /**
     * 둔화 상태 효과 클래스.
     */
    private static final class QuakerA2Slow extends Slow {
        private static final QuakerA2Slow instance = new QuakerA2Slow();

        private QuakerA2Slow() {
            super(QuakerA2Info.SLOW);
        }
    }

    private final class QuakerA2Effect extends Hitscan<CombatEntity> {
        private QuakerA2Effect() {
            super(combatUser, CombatUtil.EntityCondition.all(), Option.builder().maxDistance(QuakerWeaponInfo.DISTANCE).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            QuakerWeaponInfo.PARTICLE.BULLET_TRAIL_DECO.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(6, location -> {
                if (getTravelDistance() <= 1)
                    return;

                Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
                QuakerWeaponInfo.PARTICLE.BULLET_TRAIL_CORE.play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<CombatEntity> getHitEntityHandler() {
            return (location, target) -> true;
        }
    }

    private final class QuakerA2Projectile extends Projectile<Damageable> {
        private final HashSet<Damageable> targets;

        private QuakerA2Projectile(@NonNull HashSet<Damageable> targets) {
            super(combatUser, QuakerA2Info.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
                    Option.builder().size(QuakerA2Info.SIZE).maxDistance(QuakerA2Info.DISTANCE).build());
            this.targets = targets;
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGroundIntervalHandler())
                    .next(createPeriodIntervalHandler(10, location -> {
                        Block floor = location.clone().subtract(0, 0.5, 0).getBlock();
                        CombatEffectUtil.playHitBlockParticle(location, floor, 3);
                        QuakerA2Info.PARTICLE.BULLET_TRAIL.play(location);
                    }));
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
                    if (target.getDamageModule().damage(this, QuakerA2Info.DAMAGE, DamageType.NORMAL, location, false, true)) {
                        target.getStatusEffectModule().apply(Stun.getInstance(), combatUser, QuakerA2Info.STUN_DURATION);
                        target.getStatusEffectModule().apply(QuakerA2Slow.instance, combatUser, QuakerA2Info.SLOW_DURATION);

                        if (target instanceof CombatUser) {
                            combatUser.addScore("적 기절시킴", QuakerA2Info.DAMAGE_SCORE);
                            bonusScoreModule.addTarget((CombatUser) target, QuakerA2Info.SLOW_DURATION);
                        }
                    }

                    QuakerA2Info.PARTICLE.HIT_ENTITY.play(location);
                }

                return !(target instanceof Barrier);
            };
        }
    }
}