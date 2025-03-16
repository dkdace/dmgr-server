package com.dace.dmgr.combat.combatant.jager.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.Summonable;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.action.skill.module.EntityModule;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.AttackModule;
import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.entity.module.ReadyTimeModule;
import com.dace.dmgr.combat.entity.module.StatusEffectModule;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.BouncingProjectile;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Getter
public final class JagerUlt extends UltimateSkill implements Summonable<JagerUlt.JagerUltEntity>, HasBonusScore {
    /** 소환 엔티티 모듈 */
    @NonNull
    private final EntityModule<JagerUltEntity> entityModule;
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public JagerUlt(@NonNull CombatUser combatUser) {
        super(combatUser, JagerUltInfo.getInstance(), Timespan.MAX, JagerUltInfo.COST);

        this.entityModule = new EntityModule<>(this);
        this.bonusScoreModule = new BonusScoreModule(this, "궁극기 보너스", JagerUltInfo.KILL_SCORE);
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
        combatUser.getWeapon().cancel();
        combatUser.setGlobalCooldown(JagerUltInfo.READY_DURATION);

        entityModule.disposeEntity();

        JagerUltInfo.SOUND.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new JagerUltProjectile().shot(loc);

            CombatEffectUtil.THROW_SOUND.play(loc);

            cancel();
        }, JagerUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    private final class JagerUltProjectile extends BouncingProjectile<Damageable> {
        private JagerUltProjectile() {
            super(combatUser, JagerUltInfo.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
                    Projectile.Option.builder().duration(Timespan.ofSeconds(5)).build(),
                    Option.builder().bounceVelocityMultiplier(0.35).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            entityModule.set(new JagerUltEntity(location));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(8, JagerUltInfo.PARTICLE.BULLET_TRAIL::play));
        }

        @Override
        @NonNull
        protected HitBlockHandler getPreHitBlockHandler() {
            return createDestroyOnGroundHitBlockHandler((location, hitBlock) -> true);
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getPreHitEntityHandler() {
            return (location, target) -> true;
        }
    }

    /**
     * 눈폭풍 발생기 클래스.
     */
    @Getter
    public final class JagerUltEntity extends SummonEntity<ArmorStand> implements HasReadyTime, Damageable, Attacker {
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

        private JagerUltEntity(@NonNull Location spawnLocation) {
            super(
                    ArmorStand.class,
                    spawnLocation,
                    combatUser.getName() + "의 눈폭풍 발생기",
                    combatUser,
                    true, true,
                    Hitbox.builder(0.7, 0.2, 0.7).offsetY(0.1).pitchFixed().build()
            );

            statusEffectModule = new StatusEffectModule(this);
            attackModule = new AttackModule();
            damageModule = new DamageModule(this, JagerUltInfo.HEALTH, true);
            readyTimeModule = new ReadyTimeModule(this, JagerUltInfo.SUMMON_DURATION);

            onInit();
        }

        private void onInit() {
            entity.setGravity(false);
            damageModule.setMaxHealth(JagerUltInfo.HEALTH);
            damageModule.setHealth(JagerUltInfo.HEALTH);

            owner.getUser().setGlowing(entity, ChatColor.WHITE);
            JagerUltInfo.SOUND.SUMMON.play(getLocation());

            addOnTick(this::onTick);
        }

        @Override
        public void onTickBeforeReady(long i) {
            if (LocationUtil.isNonSolid(getLocation().add(0, 0.2, 0)))
                entity.teleport(getLocation().add(0, 0.2, 0));

            Location loc = getLocation();
            JagerUltInfo.PARTICLE.SUMMON_BEFORE_READY_TICK.play(loc);
            JagerUltInfo.PARTICLE.DISPLAY.play(loc);
            JagerUltInfo.SOUND.SUMMON_BEFORE_READY.play(loc);
        }

        @Override
        public void onReady() {
            // 미사용
        }

        private void onTick(long i) {
            JagerUltInfo.PARTICLE.DISPLAY.play(getLocation());
            if (!readyTimeModule.isReady())
                return;

            double range = Math.min(JagerUltInfo.MIN_RADIUS + ((double) i / JagerUltInfo.MAX_RADIUS_DURATION.toTicks()) * (JagerUltInfo.MAX_RADIUS - JagerUltInfo.MIN_RADIUS),
                    JagerUltInfo.MAX_RADIUS);
            playTickEffect(i, range);

            if (i % 4 == 0)
                new JagerUltArea(range).emit(getLocation());
            if (i >= JagerUltInfo.DURATION.toTicks())
                remove();
        }

        /**
         * 발생기 표시 효과를 재생한다.
         *
         * @param i     인덱스
         * @param range 현재 범위. (단위: 블록)
         */
        private void playTickEffect(long i, double range) {
            Location loc = getLocation();
            if (i <= JagerUltInfo.DURATION.toTicks() - 100 && i % 30 == 0)
                JagerUltInfo.SOUND.TICK.play(loc);

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

                JagerUltInfo.PARTICLE.TICK_CORE.play(loc1, vec.setY(-0.6), j / 6.0);
                JagerUltInfo.PARTICLE.TICK_CORE.play(loc2, vec.multiply(-1).setY(-0.6), j / 6.0);
                JagerUltInfo.PARTICLE.TICK_DECO.play(loc1.subtract(0, 2.5, 0));
                JagerUltInfo.PARTICLE.TICK_DECO.play(loc2.subtract(0, 2.5, 0));
            }
        }

        @Override
        public double getWidth() {
            return 0.7;
        }

        @Override
        public double getHeight() {
            return 0.2;
        }

        @Override
        public boolean isCreature() {
            return false;
        }

        @Override
        public double getScore() {
            return JagerUltInfo.DEATH_SCORE;
        }

        @Override
        public void onAttack(@NonNull Damageable victim, double damage, boolean isCrit, boolean isUlt) {
            owner.onAttack(victim, damage, isCrit, isUlt);

            if (victim instanceof CombatUser)
                bonusScoreModule.addTarget((CombatUser) victim, JagerUltInfo.KILL_SCORE_TIME_LIMIT);
        }

        @Override
        public void onKill(@NonNull Damageable victim) {
            owner.onKill(victim);
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location,
                             boolean isCrit) {
            JagerUltInfo.SOUND.DAMAGE.play(getLocation(), 1 + damage * 0.001);
            CombatEffectUtil.playBreakParticle(this, location, damage);
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            remove();

            JagerUltInfo.PARTICLE.DEATH.play(getLocation());
            JagerUltInfo.SOUND.DEATH.play(getLocation());
        }

        private final class JagerUltArea extends Area<Damageable> {
            private JagerUltArea(double radius) {
                super(JagerUltEntity.this, radius, CombatUtil.EntityCondition.enemy(JagerUltEntity.this)
                        .and(combatEntity -> combatEntity.getLocation().add(0, combatEntity.getHeight(), 0).getY()
                                < JagerUltEntity.this.getLocation().getY()));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.getDamageModule().damage(JagerUltEntity.this, JagerUltInfo.DAMAGE_PER_SECOND * 4 / 20.0, DamageType.NORMAL,
                        null, false, false))
                    JagerT1.addFreezeValue(target, JagerUltInfo.FREEZE_PER_SECOND * 4 / 20);

                return true;
            }
        }
    }
}
