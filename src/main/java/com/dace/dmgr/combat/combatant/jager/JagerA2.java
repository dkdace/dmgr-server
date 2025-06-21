package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Summonable;
import com.dace.dmgr.combat.action.skill.module.EntityModule;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AttackerModule;
import com.dace.dmgr.combat.entity.module.DamageModule;
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

        JagerA2Info.Effects.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new JagerA2Projectile().shot(loc);

            JagerA2Info.Effects.USE_READY.play(loc);
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
        protected void onDestroy(@NonNull Location location, boolean isForce) {
            if (!isForce)
                entityModule.set(new JagerA2Entity(location));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(8, JagerA2Info.Effects.BULLET_TRAIL::play));
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
    public final class JagerA2Entity extends SummonEntity<ArmorStand> implements Damageable, Attacker {
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

        private JagerA2Entity(@NonNull Location spawnLocation) {
            super(ArmorStandSpawnHandler.getInstance(), spawnLocation, combatUser.getName() + "의 곰덫", combatUser, true,
                    Hitbox.builder(0.8, 0.1, 0.8).offsetY(0.05).pitchFixed().build());

            this.attackerModule = new AttackerModule(this);
            this.damageModule = new DamageModule(this, JagerA2Info.HEALTH, true);
            this.statusEffectModule = new StatusEffectModule(this);

            onInit();
        }

        private void onInit() {
            entity.teleport(getLocation().add(0, 0.05, 0));

            owner.getUser().getGlowingManager().setGlowing(entity, ChatColor.WHITE);
            JagerA2Info.Effects.SUMMON.play(getLocation());

            addOnTick(this::onTick);
            addTask(new DelayTask(() -> {
                isReady = true;
                JagerA2Info.Effects.SUMMON_READY.play(getLocation());
            }, JagerA2Info.SUMMON_DURATION.toTicks()));
        }

        private void onTick(long i) {
            JagerA2Info.Effects.playDisplay(getLocation());

            if (!isReady) {
                JagerA2Info.Effects.SUMMON_BEFORE_READY_TICK.play(getLocation());
                return;
            }

            Damageable target = CombatEntityRegistry.getNearCombatEntity(getLocation().add(0, 0.5, 0), 0.8,
                    EntityCondition.enemy(this).and(Damageable::isCreature));

            if (target != null)
                onCatchEnemy(target);
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

            JagerA2Info.Effects.TRIGGER.play(getLocation());
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
            combatUser.getActionManager().getSkill(JagerP1Info.getInstance()).use(victim);
        }

        @Override
        public void onKill(@NonNull Damageable victim) {
            owner.onKill(victim);
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit) {
            JagerA2Info.Effects.DAMAGE.apply(this, location, damage).play(CombatEffectUtil.getHitLocation(this, location));
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            remove();
            JagerA2Info.Effects.DEATH.play(getLocation());
        }
    }
}
