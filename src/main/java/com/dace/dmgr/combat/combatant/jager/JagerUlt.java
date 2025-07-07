package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.skill.HasBonusScore;
import com.dace.dmgr.combat.ability.skill.UltimateSkill;
import com.dace.dmgr.combat.ability.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.ability.skill.module.SummonModule;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AttackerModule;
import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.entity.module.StatusEffectModule;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.entity.temporary.spawnhandler.ArmorStandSpawnHandler;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.BouncingProjectile;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.Nullable;

public final class JagerUlt extends UltimateSkill implements HasBonusScore {
    /** 엔티티 소환 모듈 */
    private final SummonModule<JagerUltEntity> summonModule;
    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;

    public JagerUlt(@NonNull CombatUser combatUser, @NonNull JagerUltInfo skillInfo) {
        super(combatUser, skillInfo, Timespan.MAX, JagerUltInfo.COST);

        this.summonModule = new SummonModule<>(this);
        this.bonusScoreModule = new BonusScoreModule(this);
    }

    @Override
    protected boolean canUse() {
        AbilityManager abilityManager = combatUser.getAbilityManager();
        return super.canUse() && isDurationFinished() && !abilityManager.getAbility(JagerA1Info.getInstance()).getConfirmModule().isChecking()
                && abilityManager.getAbility(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onSlot() {
        setDuration();

        combatUser.getAbilityManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(JagerUltInfo.READY_DURATION);

        summonModule.removeEntity();

        JagerUltInfo.Effects.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new JagerUltProjectile().shot(loc);

            JagerUltInfo.Effects.USE_READY.play(loc);
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

    /**
     * 눈폭풍 발생기가 생성된 상태인지 확인한다.
     *
     * @return 생성 여부
     */
    boolean isEntityCreated() {
        return summonModule.get() != null;
    }

    @Override
    @NonNull
    public CombatScore getCombatScore() {
        return JagerUltInfo.KILL_SCORE;
    }

    private final class JagerUltProjectile extends BouncingProjectile<Damageable> {
        private JagerUltProjectile() {
            super(JagerUlt.this, JagerUltInfo.VELOCITY, EntityCondition.enemy(combatUser),
                    Projectile.Option.builder().duration(Timespan.ofSeconds(5)).build(),
                    Option.builder().bounceVelocityMultiplier(0.35).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location, boolean isForce) {
            if (!isForce)
                summonModule.set(new JagerUltEntity(location));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(8, JagerUltInfo.Effects.BULLET_TRAIL::play));
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
    private final class JagerUltEntity extends SummonEntity<ArmorStand> implements Damageable, Attacker {
        /** 공격 모듈 */
        @NonNull
        @Getter
        private final AttackerModule attackerModule;
        /** 피해 모듈 */
        @NonNull
        @Getter
        private final DamageModule damageModule;
        /** 상태 효과 모듈 */
        @NonNull
        @Getter
        private final StatusEffectModule statusEffectModule;
        /** 준비 완료 여부 */
        private boolean isReady = false;

        private JagerUltEntity(@NonNull Location spawnLocation) {
            super(ArmorStandSpawnHandler.getInstance(), spawnLocation, combatUser.getName() + "의 눈폭풍 발생기", combatUser, true,
                    Hitbox.builder(0.7, 0.2, 0.7).offsetY(0.1).pitchFixed().build());

            this.attackerModule = new AttackerModule(this);
            this.damageModule = new DamageModule(this, JagerUltInfo.HEALTH, true);
            this.statusEffectModule = new StatusEffectModule(this);

            onInit();
        }

        private void onInit() {
            entity.setGravity(false);

            owner.getUser().getGlowingManager().setGlowing(entity, ChatColor.WHITE);
            JagerUltInfo.Effects.SUMMON.play(getLocation());

            addOnTick(this::onTick);
            addTask(new DelayTask(() -> isReady = true, JagerUltInfo.SUMMON_DURATION.toTicks()));
        }

        private void onTick(long i) {
            JagerUltInfo.Effects.DISPLAY.play(getLocation());

            if (!isReady) {
                if (LocationUtil.isNonSolid(getLocation().add(0, 0.2, 0)))
                    entity.teleport(getLocation().add(0, 0.2, 0));

                JagerUltInfo.Effects.SUMMON_BEFORE_READY_TICK.play(getLocation());

                return;
            }

            double minRadius = JagerUltInfo.MIN_RADIUS;
            double maxRadius = JagerUltInfo.MAX_RADIUS;
            double range = Math.min(minRadius + ((double) i / JagerUltInfo.MAX_RADIUS_DURATION.toTicks()) * (maxRadius - minRadius), maxRadius);

            JagerUltInfo.Effects.playTick(i, getLocation(), range);

            if (i % 4 == 0)
                new JagerUltArea(range).emit(getLocation());

            if (i >= JagerUltInfo.DURATION.toTicks())
                remove();
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
        public int getScore() {
            return JagerUltInfo.DEATH_SCORE;
        }

        @Override
        public void onAttack(@NonNull Damageable victim, double damage, boolean isCrit, boolean isUlt) {
            owner.onAttack(victim, damage, isCrit, isUlt);

            if (victim.isGoalTarget())
                bonusScoreModule.addTarget(victim, JagerUltInfo.KILL_SCORE_TIME_LIMIT);
        }

        @Override
        public void onKill(@NonNull Damageable victim) {
            owner.onKill(victim);
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit) {
            JagerUltInfo.Effects.DAMAGE.apply(this, location, damage).play(CombatEffectUtil.getHitLocation(this, location));
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            remove();
            JagerUltInfo.Effects.DEATH.play(getLocation());
        }

        private final class JagerUltArea extends Area<Damageable> {
            private JagerUltArea(double radius) {
                super(JagerUltEntity.this, radius, EntityCondition.enemy(JagerUltEntity.this).and(combatEntity ->
                        combatEntity.getLocation().add(0, combatEntity.getHeight(), 0).getY() < JagerUltEntity.this.getLocation().getY()));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.getDamageModule().damage(JagerUltEntity.this, JagerUltInfo.DAMAGE_PER_SECOND * 4 / 20.0, DamageType.NORMAL,
                        null, false, false))
                    JagerT1.addValue(target, JagerUltInfo.FREEZE_PER_SECOND * 4 / 20);

                return true;
            }
        }
    }
}
