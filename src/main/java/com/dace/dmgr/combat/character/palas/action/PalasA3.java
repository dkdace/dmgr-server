package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.WeakHashMap;

public final class PalasA3 extends ActiveSkill {
    /** 처치 지원 점수 제한시간 타임스탬프 목록 (피격자 : 종료 시점) */
    private final WeakHashMap<CombatUser, Timestamp> assistScoreTimeLimitTimestampMap = new WeakHashMap<>();

    public PalasA3(@NonNull CombatUser combatUser) {
        super(combatUser, PalasA3Info.getInstance(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public long getDefaultCooldown() {
        return PalasA3Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown((int) PalasA3Info.READY_DURATION);

        PalasA3Info.SOUND.USE.play(combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            onCancelled();

            Location loc = combatUser.getArmLocation(true);
            new PalasA3Projectile().shot(loc);

            CombatEffectUtil.THROW_SOUND.play(loc);
        }, PalasA3Info.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
    }

    /**
     * 플레이어에게 처치 지원 점수를 지급한다.
     *
     * @param victim 피격자
     */
    public void applyAssistScore(@NonNull CombatUser victim) {
        Timestamp expiration = assistScoreTimeLimitTimestampMap.get(victim);
        if (expiration != null && expiration.isAfter(Timestamp.now()))
            combatUser.addScore("처치 지원", PalasA3Info.ASSIST_SCORE);
    }

    /**
     * 체력 증가 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class PalasA3HealthIncrease implements StatusEffect {
        /** 증가한 최대 체력 */
        private int increasedHealth;

        @Override
        @NonNull
        public StatusEffectType getStatusEffectType() {
            return StatusEffectType.NONE;
        }

        @Override
        public boolean isPositive() {
            return true;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            int maxHealth = combatEntity.getDamageModule().getMaxHealth();
            int newMaxHealth = (int) (maxHealth * (1 + PalasA3Info.HEALTH_INCREASE_RATIO));
            increasedHealth = newMaxHealth - maxHealth;

            combatEntity.getDamageModule().setMaxHealth(newMaxHealth);
            combatEntity.getDamageModule().setHealth(combatEntity.getDamageModule().getHealth() + increasedHealth);

            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§a§l최대 체력 증가", "", Timespan.ZERO, Timespan.ofTicks(5),
                        Timespan.ofTicks(10));
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().setMaxHealth(combatEntity.getDamageModule().getMaxHealth() - increasedHealth);
        }
    }

    /**
     * 체력 감소 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class PalasA3HealthDecrease implements StatusEffect {
        /** 감소한 최대 체력 */
        private int decreasedHealth;

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
            int maxHealth = combatEntity.getDamageModule().getMaxHealth();
            int newMaxHealth = (int) (maxHealth * (1 - PalasA3Info.HEALTH_DECREASE_RATIO));
            decreasedHealth = maxHealth - newMaxHealth;

            combatEntity.getDamageModule().setMaxHealth(newMaxHealth);

            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§c§l최대 체력 감소", "", Timespan.ZERO, Timespan.ofTicks(5),
                        Timespan.ofTicks(10));
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().setMaxHealth(combatEntity.getDamageModule().getMaxHealth() + decreasedHealth);
        }
    }

    private final class PalasA3Projectile extends Projectile<Damageable> {
        private PalasA3Projectile() {
            super(combatUser, PalasA3Info.VELOCITY,
                    CombatUtil.EntityCondition.enemy(combatUser).or(CombatUtil.EntityCondition.team(combatUser).exclude(combatUser)));
        }

        @Override
        protected void onHit(@NonNull Location location) {
            Location loc = location.add(0, 0.1, 0);
            new PalasA3Area().emit(loc);

            PalasA3Info.SOUND.EXPLODE.play(loc);
            PalasA3Info.PARTICLE.EXPLODE.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(8, PalasA3Info.PARTICLE.BULLET_TRAIL::play));
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
                super(combatUser, PalasA3Info.RADIUS, CombatUtil.EntityCondition.of(Damageable.class));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.getDamageModule().isLiving()) {
                    if (target.isEnemy(combatUser)) {
                        if (target.getDamageModule().damage(PalasA3Projectile.this, 1, DamageType.NORMAL, null,
                                false, true)) {
                            target.getStatusEffectModule().applyStatusEffect(combatUser, new PalasA3HealthDecrease(), PalasA3Info.DURATION);

                            if (target instanceof CombatUser)
                                assistScoreTimeLimitTimestampMap.put((CombatUser) target, Timestamp.now().plus(Timespan.ofTicks(PalasA3Info.DURATION)));
                        }
                    } else if (target instanceof Healable) {
                        target.getStatusEffectModule().applyStatusEffect(combatUser, new PalasA3HealthIncrease(), PalasA3Info.DURATION);

                        if (target instanceof CombatUser && target != combatUser) {
                            combatUser.addScore("생체 제어 수류탄", PalasA3Info.EFFECT_SCORE);
                            ((CombatUser) target).addKillAssist(combatUser, PalasA3.this, PalasA3Info.ASSIST_SCORE, PalasA3Info.DURATION);
                        }

                        return true;
                    }
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
