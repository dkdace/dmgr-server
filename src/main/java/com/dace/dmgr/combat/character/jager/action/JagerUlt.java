package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.HasReadyTime;
import com.dace.dmgr.combat.entity.module.*;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.interaction.*;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Getter
public final class JagerUlt extends UltimateSkill {
    /** 처치 점수 제한시간 쿨타임 ID */
    private static final String KILL_SCORE_COOLDOWN_ID = "JagerUltKillScoreTimeLimit";
    /** 소환한 엔티티 */
    @Nullable
    private JagerUltEntity summonEntity = null;

    public JagerUlt(@NonNull CombatUser combatUser) {
        super(combatUser, JagerUltInfo.getInstance());
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public int getCost() {
        return JagerUltInfo.COST;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && !combatUser.getSkill(JagerA1Info.getInstance()).getConfirmModule().isChecking()
                && combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown((int) JagerUltInfo.READY_DURATION);
        if (summonEntity != null)
            summonEntity.dispose();

        SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_ULT_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            Location loc = combatUser.getArmLocation(true);
            new JagerUltProjectile().shoot(loc);

            SoundUtil.playNamedSound(NamedSound.COMBAT_THROW, loc);

            onCancelled();
        }, JagerUltInfo.READY_DURATION));
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

    @Override
    public void reset() {
        super.reset();

        if (summonEntity != null)
            summonEntity.dispose();
    }

    /**
     * 플레이어에게 보너스 점수를 지급한다.
     *
     * @param victim 피격자
     * @param score  점수 (처치 기여도)
     */
    public void applyBonusScore(@NonNull CombatUser victim, int score) {
        if (CooldownUtil.getCooldown(combatUser, KILL_SCORE_COOLDOWN_ID + victim) > 0)
            combatUser.addScore("궁극기 보너스", JagerUltInfo.KILL_SCORE * score / 100.0);
    }

    private final class JagerUltProjectile extends BouncingProjectile {
        private JagerUltProjectile() {
            super(combatUser, JagerUltInfo.VELOCITY, -1, ProjectileOption.builder().trailInterval(8).duration(100).hasGravity(true)
                    .condition(combatUser::isEnemy).build(), BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35)
                    .destroyOnHitFloor(true).build());
        }

        @Override
        protected void onTrailInterval() {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, getLocation(), 15,
                    0.6, 0.02, 0.6, 96, 220, 255);
        }

        @Override
        protected void onHitBlockBouncing(@NonNull Block hitBlock) {
            // 미사용
        }

        @Override
        protected boolean onHitEntityBouncing(@NonNull Damageable target, boolean isCrit) {
            return false;
        }

        @Override
        protected void onDestroy() {
            summonEntity = new JagerUltEntity(CombatUtil.spawnEntity(ArmorStand.class, getLocation()), combatUser);
            summonEntity.activate();
        }
    }

    /**
     * 눈폭풍 발생기 클래스.
     */
    @Getter
    public final class JagerUltEntity extends SummonEntity<ArmorStand> implements HasReadyTime, Damageable, Attacker {
        /** 넉백 모듈 */
        @NonNull
        private final KnockbackModule knockbackModule;
        /** 상태 효과 모듈 */
        @NonNull
        private final StatusEffectModule statusEffectModule;
        /** 공격 모듈 */
        @NonNull
        private final AttackModule attackModule;
        /** 피해 모듈 */
        @NonNull
        private final DamageModule damageModule;
        /** 준비 시간 모듈 */
        @NonNull
        private final ReadyTimeModule readyTimeModule;

        private JagerUltEntity(@NonNull ArmorStand entity, @NonNull CombatUser owner) {
            super(
                    entity,
                    owner.getName() + "의 눈폭풍 발생기",
                    owner,
                    true, true,
                    new FixedPitchHitbox(entity.getLocation(), 0.7, 0.2, 0.7, 0, 0.1, 0)
            );

            knockbackModule = new KnockbackModule(this, 2);
            statusEffectModule = new StatusEffectModule(this);
            attackModule = new AttackModule(this);
            damageModule = new DamageModule(this, false, true, false, JagerUltInfo.DEATH_SCORE, JagerUltInfo.HEALTH);
            readyTimeModule = new ReadyTimeModule(this, JagerUltInfo.SUMMON_DURATION);

            onInit();
        }

        private void onInit() {
            entity.setAI(false);
            entity.setGravity(false);
            entity.setSilent(true);
            entity.setInvulnerable(true);
            entity.setMarker(true);
            entity.setSmall(true);
            entity.setVisible(false);
            damageModule.setMaxHealth(JagerUltInfo.HEALTH);
            damageModule.setHealth(JagerUltInfo.HEALTH);

            GlowUtil.setGlowing(entity, ChatColor.WHITE, owner.getEntity());
            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_ULT_SUMMON, entity.getLocation());
        }

        @Override
        public void activate() {
            super.activate();
            readyTimeModule.ready();
        }

        @Override
        public void onTickBeforeReady(long i) {
            if (LocationUtil.isNonSolid(entity.getLocation().add(0, 0.2, 0)))
                entity.teleport(entity.getLocation().add(0, 0.2, 0));

            Location loc = entity.getLocation();
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc, 0, 0, -1, 0, 0.3);
            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_ULT_SUMMON_BEFORE_READY, loc);
            playTickEffect();
        }

        @Override
        public void onReady() {
            // 미사용
        }

        @Override
        protected void onTick(long i) {
            super.onTick(i);

            playTickEffect();
            if (!readyTimeModule.isReady())
                return;

            double range = Math.min(JagerUltInfo.MIN_RADIUS + ((double) i / JagerUltInfo.MAX_RADIUS_DURATION) * (JagerUltInfo.MAX_RADIUS - JagerUltInfo.MIN_RADIUS),
                    JagerUltInfo.MAX_RADIUS);
            playTickEffect(i, range);

            if (i % 4 == 0)
                new JagerUltArea(range).emit(entity.getLocation());
            if (i >= JagerUltInfo.DURATION)
                dispose();
        }

        /**
         * 발생기 표시 효과를 재생한다.
         */
        private void playTickEffect() {
            Location loc = entity.getLocation();
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 8, 0.6, 0.02, 0.6,
                    96, 220, 255);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 3, 0.15, 0.02, 0.15,
                    80, 80, 100);
        }

        /**
         * 발생기 표시 효과를 재생한다.
         *
         * @param i     인덱스
         * @param range 현재 범위. (단위: 블록)
         */
        private void playTickEffect(long i, double range) {
            Location loc = entity.getLocation();
            if (i <= JagerUltInfo.DURATION - 100 && i % 30 == 0)
                SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_ULT_TICK, loc);

            loc.setYaw(0);
            loc.setPitch(0);
            Vector vector = VectorUtil.getRollAxis(loc);
            Vector axis = VectorUtil.getYawAxis(loc);

            long angle = i * 14;
            for (int j = 1; j <= 6; j++) {
                angle += 19;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);
                Location loc1 = loc.clone().add(vec.clone().multiply(range / 6 * j));
                Location loc2 = loc.clone().subtract(vec.clone().multiply(range / 6 * j));

                ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc1, 0, vec.getX(), -0.6, vec.getZ(), 0.05 * (7 - j));
                ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc2, 0, vec.getX(), 0.6, vec.getZ(), -0.05 * (7 - j));
                ParticleUtil.play(Particle.SNOW_SHOVEL, loc1.subtract(0, 2.5, 0), 3, 0, 1.4, 0, 0.04);
                ParticleUtil.play(Particle.SNOW_SHOVEL, loc2.subtract(0, 2.5, 0), 3, 0, 1.4, 0, 0.04);
            }
        }

        @Override
        public void dispose() {
            super.dispose();

            summonEntity = null;
        }

        @Override
        public void onAttack(@NonNull Damageable victim, int damage, @NonNull DamageType damageType, boolean isCrit, boolean isUlt) {
            owner.onAttack(victim, damage, damageType, isCrit, isUlt);

            if (victim instanceof CombatUser)
                CooldownUtil.setCooldown(combatUser, KILL_SCORE_COOLDOWN_ID + victim, JagerUltInfo.KILL_SCORE_TIME_LIMIT);
        }

        @Override
        public void onKill(@NonNull Damageable victim) {
            owner.onKill(victim);
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, @Nullable Location location,
                             boolean isCrit, boolean isUlt) {
            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_ULT_DAMAGE, entity.getLocation(), 1 + damage * 0.001);
            CombatEffectUtil.playBreakEffect(location, entity, damage);
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            dispose();

            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0, entity.getLocation(), 120,
                    0.1, 0.1, 0.1, 0.15);
            ParticleUtil.play(Particle.CRIT, entity.getLocation(), 80, 0.1, 0.1, 0.1, 0.5);
            ParticleUtil.play(Particle.EXPLOSION_LARGE, entity.getLocation(), 1, 0, 0, 0, 0);
            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_ULT_DEATH, entity.getLocation());
        }

        private final class JagerUltArea extends Area {
            private JagerUltArea(double radius) {
                super(JagerUltEntity.this, radius, combatEntity -> combatEntity.isEnemy(JagerUltEntity.this)
                        && combatEntity.getEntity().getLocation().add(0, combatEntity.getEntity().getHeight(), 0).getY()
                        < JagerUltEntity.this.entity.getLocation().getY());
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.getDamageModule().damage(JagerUltEntity.this, JagerUltInfo.DAMAGE_PER_SECOND * 4 / 20, DamageType.NORMAL,
                        null, false, false))
                    JagerT1.addFreezeValue(target, JagerUltInfo.FREEZE_PER_SECOND * 4 / 20);

                return true;
            }
        }
    }
}
