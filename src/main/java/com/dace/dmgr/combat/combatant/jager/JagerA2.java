package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Summonable;
import com.dace.dmgr.combat.action.skill.module.EntityModule;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AttackModule;
import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.entity.module.ReadyTimeModule;
import com.dace.dmgr.combat.entity.module.StatusEffectModule;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.entity.temporary.spawnhandler.ArmorStandSpawnHandler;
import com.dace.dmgr.combat.interaction.BouncingProjectile;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.Nullable;

@Getter
public final class JagerA2 extends ActiveSkill implements Summonable<JagerA2.JagerA2Entity> {
    /** 소환 엔티티 모듈 */
    @NonNull
    private final EntityModule<JagerA2Entity> entityModule;

    public JagerA2(@NonNull CombatUser combatUser) {
        super(combatUser, JagerA2Info.getInstance(), JagerA2Info.COOLDOWN, Timespan.MAX, 1);
        this.entityModule = new EntityModule<>(this);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ActionManager actionManager = combatUser.getActionManager();
        return super.canUse(actionKey) && isDurationFinished() && !actionManager.getSkill(JagerA1Info.getInstance()).getConfirmModule().isChecking()
                && actionManager.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.getActionManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(JagerA2Info.READY_DURATION);

        entityModule.removeEntity();

        JagerA2Info.Sounds.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new JagerA2Projectile().shot(loc);

            CombatEffectUtil.THROW_SOUND.play(loc);
        }, JagerA2Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    private final class JagerA2Projectile extends BouncingProjectile<Damageable> {
        private JagerA2Projectile() {
            super(JagerA2.this, JagerA2Info.VELOCITY, EntityCondition.enemy(combatUser),
                    Projectile.Option.builder().duration(Timespan.ofSeconds(5)).build(),
                    Option.builder().bounceVelocityMultiplier(0.35).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            entityModule.set(new JagerA2Entity(location));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(8, JagerA2Info.Particles.BULLET_TRAIL::play));
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
     * 곰덫 클래스.
     */
    @Getter
    public final class JagerA2Entity extends SummonEntity<ArmorStand> implements HasReadyTime, Damageable, Attacker {
        /** 공격 모듈 */
        @NonNull
        private final AttackModule attackModule;
        /** 피해 모듈 */
        @NonNull
        private final DamageModule damageModule;
        /** 상태 효과 모듈 */
        @NonNull
        private final StatusEffectModule statusEffectModule;
        /** 준비 시간 모듈 */
        @NonNull
        private final ReadyTimeModule readyTimeModule;

        private JagerA2Entity(@NonNull Location spawnLocation) {
            super(ArmorStandSpawnHandler.getInstance(), spawnLocation, combatUser.getName() + "의 곰덫", combatUser, true,
                    Hitbox.builder(0.8, 0.1, 0.8).offsetY(0.05).pitchFixed().build());

            this.attackModule = new AttackModule();
            this.damageModule = new DamageModule(this, JagerA2Info.HEALTH, true);
            this.statusEffectModule = new StatusEffectModule(this);
            this.readyTimeModule = new ReadyTimeModule(this, JagerA2Info.SUMMON_DURATION);

            onInit();
        }

        private void onInit() {
            entity.teleport(getLocation().add(0, 0.05, 0));

            owner.getUser().getGlowingManager().setGlowing(entity, ChatColor.WHITE);
            JagerA2Info.Sounds.SUMMON.play(getLocation());

            addOnTick(this::onTick);
        }

        @Override
        public void onTickBeforeReady(long i) {
            JagerA2Info.Particles.SUMMON_BEFORE_READY_TICK.play(getLocation());
        }

        @Override
        public void onReady() {
            JagerA2Info.Sounds.SUMMON_READY.play(getLocation());
        }

        private void onTick(long i) {
            playTickEffect();
            if (!readyTimeModule.isReady())
                return;

            Damageable target = CombatUtil.getNearCombatEntity(getLocation().add(0, 0.5, 0), 0.8,
                    EntityCondition.enemy(this).and(Damageable::isCreature));

            if (target != null)
                onCatchEnemy(target);
        }

        /**
         * 덫 표시 효과를 재생한다.
         */
        private void playTickEffect() {
            for (int i = 0; i < 7; i++) {
                JagerA2Info.Particles.DISPLAY.play(getLocation().add(i % 2 == 0 ? 0.4 : 0.55, 0, 0.6 - i * 0.2));
                JagerA2Info.Particles.DISPLAY.play(getLocation().add(i % 2 == 0 ? -0.4 : -0.55, 0, 0.6 - i * 0.2));
            }
            for (int i = 0; i < 5; i++)
                JagerA2Info.Particles.DISPLAY.play(getLocation().add(0, 0, 0.4 - i * 0.2));
        }

        /**
         * 덫 발동 시 실행할 작업.
         *
         * @param target 대상 엔티티
         */
        private void onCatchEnemy(@NonNull Damageable target) {
            if (target.getDamageModule().damage(this, JagerA2Info.DAMAGE, DamageType.NORMAL, target.getLocation().add(0, 0.2, 0),
                    false, true)) {
                target.getStatusEffectModule().apply(Snare.getInstance(), JagerA2Info.SNARE_DURATION);

                if (target.isGoalTarget())
                    combatUser.addScore("곰덫", JagerA2Info.SNARE_SCORE);
            }

            JagerA2Info.Sounds.TRIGGER.play(getLocation());
            remove();
        }

        @Override
        public double getWidth() {
            return 0.8;
        }

        @Override
        public double getHeight() {
            return 0.1;
        }

        @Override
        public boolean isCreature() {
            return false;
        }

        @Override
        public int getScore() {
            return JagerA2Info.DEATH_SCORE;
        }

        @Override
        public void onAttack(@NonNull Damageable victim, double damage, boolean isCrit, boolean isUlt) {
            owner.onAttack(victim, damage, isCrit, isUlt);

            ActionManager actionManager = combatUser.getActionManager();
            actionManager.getSkill(JagerP1Info.getInstance()).setTarget(victim);
            actionManager.useAction(ActionKey.PERIODIC_1);
        }

        @Override
        public void onKill(@NonNull Damageable victim) {
            owner.onKill(victim);
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit) {
            JagerA2Info.Sounds.DAMAGE.play(getLocation(), 1 + damage * 0.001);
            CombatEffectUtil.playBreakParticle(this, location, damage);
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            remove();

            JagerA2Info.Particles.DEATH.play(getLocation());
            JagerA2Info.Sounds.DEATH.play(getLocation());
        }
    }
}
