package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.character.palas.Palas;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;

@Getter
public final class PalasA3 extends ActiveSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    private static final String ASSIST_SCORE_COOLDOWN_ID = "PalasA3AssistScoreTimeLimit";

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
            new PalasA3Projectile().shoot(loc);

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
        if (CooldownUtil.getCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + victim) > 0)
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
                ((CombatUser) combatEntity).getUser().sendTitle("§a§l최대 체력 증가", "", 0, 5, 10);
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
                ((CombatUser) combatEntity).getUser().sendTitle("§c§l최대 체력 감소", "", 0, 5, 10);
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

    private final class PalasA3Projectile extends Projectile {
        private PalasA3Projectile() {
            super(combatUser, PalasA3Info.VELOCITY, ProjectileOption.builder().trailInterval(8).hasGravity(true).condition(combatEntity ->
                    Palas.getTargetedActionCondition(PalasA3.this.combatUser, combatEntity) || combatEntity.isEnemy(PalasA3.this.combatUser)).build());
        }

        @Override
        protected void onTrailInterval() {
            PalasA3Info.PARTICLE.BULLET_TRAIL.play(getLocation());
        }

        @Override
        protected void onHit() {
            Location loc = getLocation().clone().add(0, 0.1, 0);
            new PalasA3Area().emit(loc);

            PalasA3Info.SOUND.EXPLODE.play(loc);
            PalasA3Info.PARTICLE.EXPLODE.play(loc);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            return false;
        }

        private final class PalasA3Area extends Area {
            private PalasA3Area() {
                super(combatUser, PalasA3Info.RADIUS, Damageable.class::isInstance);
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.getDamageModule().isLiving()) {
                    if (target.isEnemy(combatUser)) {
                        if (target.getDamageModule().damage(PalasA3Projectile.this, 1, DamageType.NORMAL, null,
                                false, true)) {
                            target.getStatusEffectModule().applyStatusEffect(combatUser, new PalasA3HealthDecrease(), PalasA3Info.DURATION);

                            if (target instanceof CombatUser)
                                CooldownUtil.setCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + target, PalasA3Info.DURATION);
                        }
                    } else if (target instanceof Healable) {
                        target.getStatusEffectModule().applyStatusEffect(combatUser, new PalasA3HealthIncrease(), PalasA3Info.DURATION);

                        if (target instanceof CombatUser && target != combatUser) {
                            combatUser.addScore("생체 제어 수류탄", PalasA3Info.EFFECT_SCORE);
                            ((CombatUser) target).addKillAssist(combatUser, PalasA3.ASSIST_SCORE_COOLDOWN_ID, PalasA3Info.ASSIST_SCORE, PalasA3Info.DURATION);
                        }

                        return true;
                    }
                }

                return !(target instanceof Barrier);
            }
        }
    }

}
