package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;

@Getter
public final class PalasA3 extends ActiveSkill implements HasBonusScore {
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public PalasA3(@NonNull CombatUser combatUser) {
        super(combatUser, PalasA3Info.getInstance(), PalasA3Info.COOLDOWN, Timespan.MAX, 2);
        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", PalasA3Info.ASSIST_SCORE);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.getActionManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(PalasA3Info.READY_DURATION);

        PalasA3Info.Sounds.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new PalasA3Projectile().shot(loc);

            CombatEffectUtil.THROW_SOUND.play(loc);
        }, PalasA3Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    /**
     * 체력 증가 상태 효과 클래스.
     */
    private final class PalasA3HealthIncrease extends StatusEffect {
        /** 증가한 최대 체력 */
        private int increasedMaxHealth;

        private PalasA3HealthIncrease() {
            super(true);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            if (!(combatEntity instanceof Healable))
                return;

            int maxHealth = combatEntity.getDamageModule().getMaxHealth();
            int newMaxHealth = (int) (maxHealth * (1 + PalasA3Info.HEALTH_INCREASE_RATIO));
            increasedMaxHealth = newMaxHealth - maxHealth;

            combatEntity.getDamageModule().setMaxHealth(newMaxHealth);
            ((Healable) combatEntity).getDamageModule().heal(combatUser, increasedMaxHealth, true);

            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§a§l최대 체력 증가", "", Timespan.ZERO, Timespan.ofTicks(5),
                        Timespan.ofTicks(10));
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            if (combatEntity instanceof Healable)
                combatEntity.getDamageModule().setMaxHealth(combatEntity.getDamageModule().getMaxHealth() - increasedMaxHealth);
        }
    }

    /**
     * 체력 감소 상태 효과 클래스.
     */
    private final class PalasA3HealthDecrease extends StatusEffect {
        /** 감소한 최대 체력 */
        private int decreasedMaxHealth;

        private PalasA3HealthDecrease() {
            super(false);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            double health = combatEntity.getDamageModule().getHealth();
            int maxHealth = combatEntity.getDamageModule().getMaxHealth();
            int newMaxHealth = (int) (maxHealth * (1 - PalasA3Info.HEALTH_DECREASE_RATIO));
            double damage = Math.max(0, health - newMaxHealth);
            decreasedMaxHealth = maxHealth - newMaxHealth;

            combatEntity.getDamageModule().damage(combatUser, damage, DamageType.FIXED, null, false, true);
            combatEntity.getDamageModule().setMaxHealth(newMaxHealth);

            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§c§l최대 체력 감소", "", Timespan.ZERO, Timespan.ofTicks(5),
                        Timespan.ofTicks(10));
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            combatEntity.getDamageModule().setMaxHealth(combatEntity.getDamageModule().getMaxHealth() + decreasedMaxHealth);
        }
    }

    private final class PalasA3Projectile extends Projectile<Damageable> {
        private PalasA3Projectile() {
            super(PalasA3.this, PalasA3Info.VELOCITY, EntityCondition.enemy(combatUser).or(EntityCondition.team(combatUser)
                    .exclude(combatUser)));
        }

        @Override
        protected void onHit(@NonNull Location location) {
            Location loc = location.add(0, 0.1, 0);
            new PalasA3Area().emit(loc);

            PalasA3Info.Sounds.EXPLODE.play(loc);
            PalasA3Info.Particles.EXPLODE.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(8, PalasA3Info.Particles.BULLET_TRAIL::play));
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> false;
        }

        private final class PalasA3Area extends Area<Damageable> {
            private PalasA3Area() {
                super(combatUser, PalasA3Info.RADIUS, EntityCondition.of(Damageable.class));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.isCreature()) {
                    if (target.isEnemy(combatUser))
                        onHitEnemy(target);
                    else if (target instanceof Healable)
                        onHitTeamer((Healable) target);

                    if (target != combatUser && target.isGoalTarget())
                        combatUser.addScore("생체 제어 수류탄", PalasA3Info.EFFECT_SCORE);
                }

                return !target.isEnemy(combatUser) || !(target instanceof Barrier);
            }

            /**
             * 적이 맞았을 때 실행할 작업.
             *
             * @param target 대상 엔티티
             */
            private void onHitEnemy(@NonNull Damageable target) {
                if (!target.getDamageModule().damage(PalasA3Projectile.this, 1, DamageType.NORMAL, null, false, true))
                    return;

                target.getStatusEffectModule().apply(new PalasA3HealthDecrease(), PalasA3Info.DURATION);

                if (target.isGoalTarget())
                    bonusScoreModule.addTarget(target, PalasA3Info.DURATION);
            }

            /**
             * 아군이 맞았을 때 실행할 작업.
             *
             * @param target 대상 엔티티
             */
            private void onHitTeamer(@NonNull Healable target) {
                target.getStatusEffectModule().apply(new PalasA3HealthIncrease(), PalasA3Info.DURATION);

                if (target instanceof CombatUser && target != combatUser)
                    ((CombatUser) target).addKillHelper(combatUser, PalasA3.this, PalasA3Info.ASSIST_SCORE, PalasA3Info.DURATION);
            }
        }
    }
}
