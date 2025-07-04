package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ChargeableSkill;
import com.dace.dmgr.combat.ability.skill.Confirmable;
import com.dace.dmgr.combat.ability.skill.HasBonusScore;
import com.dace.dmgr.combat.ability.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.ability.skill.module.LocationConfirmModule;
import com.dace.dmgr.combat.ability.skill.module.SummonModule;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.*;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.entity.temporary.spawnhandler.EntitySpawnHandler;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Wolf;
import org.jetbrains.annotations.Nullable;

public final class JagerA1 extends ChargeableSkill implements HasBonusScore, Confirmable {
    /** 엔티티 소환 모듈 */
    private final SummonModule<JagerA1Entity> summonModule;
    /** 위치 확인 모듈 */
    @NonNull
    @Getter
    private final LocationConfirmModule confirmModule;
    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;

    public JagerA1(@NonNull CombatUser combatUser, @NonNull JagerA1Info skillInfo) {
        super(combatUser, skillInfo, JagerA1Info.COOLDOWN, JagerA1Info.HEALTH);

        this.summonModule = new SummonModule<>(this);
        this.confirmModule = new LocationConfirmModule(this, JagerA1Info.SUMMON_MAX_DISTANCE);
        this.bonusScoreModule = new BonusScoreModule(this, "설랑 보너스", JagerA1Info.KILL_SCORE);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        ActionBarDisplay.Builder builder = ActionBarDisplay.builder(this).title().progressBar();

        if (isDurationFinished())
            return builder.build();
        else
            return builder.keyInfo("회수", ActionKey.SLOT_1).build();
    }

    @Override
    public double getStateValueDecrement() {
        return 0;
    }

    @Override
    public double getStateValueIncrement() {
        return JagerA1Info.HEALTH / JagerA1Info.RECOVER_DURATION.toSeconds();
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && combatUser.getAbilityManager().getAbility(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_1;
    }

    @Override
    public void onSlot() {
        if (isDurationFinished()) {
            combatUser.getAbilityManager().getWeapon().cancel();
            confirmModule.toggleCheck();
        } else {
            setDuration(Timespan.ZERO);
            summonModule.removeEntity();
        }
    }

    @Override
    public void onLeftClick() {
        confirmModule.accept();
    }

    @Override
    public void onAccept() {
        setDuration();
        combatUser.getAbilityManager().getWeapon().setCooldown(Timespan.ofTicks(2));

        summonModule.set(new JagerA1Entity(confirmModule.getCurrentLocation()));
    }

    @Override
    public boolean isCancellable() {
        return confirmModule.isChecking();
    }

    @Override
    protected void onCancelled() {
        confirmModule.cancel();
    }

    /**
     * 설랑 클래스.
     */
    public final class JagerA1Entity extends SummonEntity<Wolf> implements Damageable, Attacker, Movable {
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
        /** 이동 모듈 */
        @NonNull
        @Getter
        private final MoveModule moveModule;
        /** 넉백 모듈 */
        @NonNull
        @Getter
        private final KnockbackModule knockbackModule;
        /** 준비 완료 여부 */
        private boolean isReady = false;

        private JagerA1Entity(@NonNull Location spawnLocation) {
            super(EntitySpawnHandler.getDefaultSpawnHandler(Wolf.class), spawnLocation, combatUser.getName() + "의 설랑", combatUser,
                    true, Hitbox.builder(0.4, 0.8, 1.2).offsetY(0.4).pitchFixed().build());

            this.attackerModule = new AttackerModule(this);
            this.damageModule = new DamageModule(this, JagerA1Info.HEALTH, true);
            this.statusEffectModule = new StatusEffectModule(this);
            this.moveModule = new MoveModule(this, JagerA1Info.SPEED);
            this.knockbackModule = new KnockbackModule(this);

            onInit();
        }

        private void onInit() {
            entity.setAI(false);
            entity.setCollarColor(DyeColor.CYAN);
            entity.setTamed(true);
            entity.setSitting(true);
            entity.setOwner(owner.getEntity());
            entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
            damageModule.setHealth(getStateValue());

            owner.getUser().getGlowingManager().setGlowing(entity, ChatColor.WHITE);
            CombatEffectUtil.ENTITY_SUMMON_SOUND.play(getLocation());

            addOnTick(this::onTick);
            addTask(new DelayTask(() -> {
                isReady = true;

                entity.setAI(true);
                JagerA1Info.Effects.SUMMON_READY.play(getLocation());
            }, JagerA1Info.SUMMON_DURATION.toTicks()));
        }

        private void onTick(long i) {
            if (!isReady) {
                JagerA1Info.Effects.SUMMON_BEFORE_READY_TICK.play(getLocation());
                return;
            }
            if (i % 10 != 0)
                return;

            if (entity.getTarget() == null) {
                entity.setAngry(false);
                entity.setSitting(true);
                entity.setTamed(true);

                Damageable target = CombatEntityRegistry.getNearCombatEntity(getLocation(), JagerA1Info.ENEMY_DETECT_RADIUS,
                        EntityCondition.enemy(this).and(Damageable::isCreature));

                if (target != null) {
                    entity.setTarget(target.getEntity());
                    JagerA1Info.Effects.ENEMY_DETECT.play(getLocation());
                }
            } else {
                entity.setAngry(true);
                entity.setSitting(false);
                entity.setTamed(false);

                CombatEntity targetCombatEntity = CombatEntity.fromEntity(entity.getTarget());
                if (targetCombatEntity == null || targetCombatEntity.isRemoved()
                        || targetCombatEntity instanceof CombatUser && ((CombatUser) targetCombatEntity).isDead())
                    entity.setTarget(null);
            }

            damageModule.setMaxHealth(JagerA1Info.HEALTH);
            damageModule.setHealth(getStateValue());
        }

        @Override
        public boolean isCreature() {
            return true;
        }

        @Override
        public int getScore() {
            return JagerA1Info.DEATH_SCORE;
        }

        @Override
        public void onAttack(@NonNull Damageable victim, double damage, boolean isCrit, boolean isUlt) {
            owner.onAttack(victim, damage, isCrit, isUlt);
            combatUser.getAbilityManager().getAbility(JagerP1Info.getInstance()).use(victim);

            if (victim.isGoalTarget())
                bonusScoreModule.addTarget(victim, JagerA1Info.KILL_SCORE_TIME_LIMIT);
        }

        @Override
        public void onDefaultAttack(@NonNull Damageable victim) {
            victim.getDamageModule().damage(this, JagerA1Info.DAMAGE, DamageType.NORMAL, null,
                    victim.getStatusEffectModule().has(Snare.class), true);
        }

        @Override
        public void onKill(@NonNull Damageable victim) {
            owner.onKill(victim);
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit) {
            setStateValue(damageModule.getHealth());
            JagerA1Info.Effects.DAMAGE.apply(this, location, damage).play(CombatEffectUtil.getHitLocation(this, location));
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            remove();

            setStateValue(0);
            setCooldown(JagerA1Info.COOLDOWN_DEATH);

            JagerA1Info.Effects.DEATH.play(getLocation());
        }
    }
}
