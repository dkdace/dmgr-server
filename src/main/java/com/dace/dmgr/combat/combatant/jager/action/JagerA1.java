package com.dace.dmgr.combat.combatant.jager.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.action.skill.Confirmable;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.Summonable;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.action.skill.module.EntityModule;
import com.dace.dmgr.combat.action.skill.module.LocationConfirmModule;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.*;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.interaction.Hitbox;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Wolf;
import org.jetbrains.annotations.Nullable;

@Getter
public final class JagerA1 extends ChargeableSkill implements Confirmable, Summonable<JagerA1.JagerA1Entity>, HasBonusScore {
    /** 위치 확인 모듈 */
    @NonNull
    private final LocationConfirmModule confirmModule;
    /** 소환 엔티티 모듈 */
    @NonNull
    private final EntityModule<JagerA1Entity> entityModule;
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public JagerA1(@NonNull CombatUser combatUser) {
        super(combatUser, JagerA1Info.getInstance(), JagerA1Info.COOLDOWN, JagerA1Info.HEALTH, 0);

        confirmModule = new LocationConfirmModule(this, ActionKey.LEFT_CLICK, ActionKey.SLOT_1, JagerA1Info.SUMMON_MAX_DISTANCE);
        entityModule = new EntityModule<>(this);
        bonusScoreModule = new BonusScoreModule(this, "설랑 보너스", JagerA1Info.KILL_SCORE);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1, ActionKey.LEFT_CLICK};
    }

    @Override
    @NonNull
    public String getActionBarString() {
        String text = ActionBarStringUtil.getProgressBar(this);
        if (!isDurationFinished())
            text += ActionBarStringUtil.getKeyInfo(this, "회수");

        return text;
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
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case SLOT_1: {
                combatUser.getWeapon().onCancelled();

                if (isDurationFinished())
                    confirmModule.toggleCheck();
                else {
                    setDuration(Timespan.ZERO);
                    entityModule.disposeEntity();
                }

                break;
            }
            case LEFT_CLICK: {
                onUse();

                break;
            }
            default:
                break;
        }
    }

    @Override
    public boolean isCancellable() {
        return confirmModule.isChecking();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        confirmModule.cancel();
    }

    @Override
    public void onCheckEnable() {
        // 미사용
    }

    @Override
    public void onCheckTick(long i) {
        // 미사용
    }

    @Override
    public void onCheckDisable() {
        // 미사용
    }

    /**
     * 사용 시 실행할 작업.
     */
    private void onUse() {
        if (!confirmModule.isValid())
            return;

        setDuration();
        confirmModule.toggleCheck();
        combatUser.setGlobalCooldown(Timespan.ofTicks(1));

        entityModule.set(new JagerA1Entity(confirmModule.getCurrentLocation()));
    }

    /**
     * 설랑 클래스.
     */
    @Getter
    public final class JagerA1Entity extends SummonEntity<Wolf> implements HasReadyTime, Damageable, Attacker, Movable, CombatEntity {
        /** 상태 효과 모듈 */
        @NonNull
        private final StatusEffectModule statusEffectModule;
        /** 공격 모듈 */
        @NonNull
        private final AttackModule attackModule;
        /** 피해 모듈 */
        @NonNull
        private final DamageModule damageModule;
        /** 이동 모듈 */
        @NonNull
        private final MoveModule moveModule;
        /** 준비 대기시간 모듈 */
        @NonNull
        private final ReadyTimeModule readyTimeModule;

        private JagerA1Entity(@NonNull Location spawnLocation) {
            super(
                    Wolf.class,
                    spawnLocation,
                    combatUser.getName() + "의 설랑",
                    combatUser,
                    true, false,
                    Hitbox.builder(0.4, 0.8, 1.2).offsetY(0.4).pitchFixed().build()
            );

            statusEffectModule = new StatusEffectModule(this);
            attackModule = new AttackModule();
            damageModule = new DamageModule(this, JagerA1Info.HEALTH, true);
            moveModule = new MoveModule(this, JagerA1Info.SPEED);
            readyTimeModule = new ReadyTimeModule(this, JagerA1Info.SUMMON_DURATION);

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

            owner.getUser().setGlowing(entity, ChatColor.WHITE);
            CombatEffectUtil.ENTITY_SUMMON_SOUND.play(getLocation());

            addOnTick(this::onTick);
        }

        @Override
        public void onTickBeforeReady(long i) {
            JagerA1Info.PARTICLE.SUMMON_BEFORE_READY_TICK.play(getLocation());
        }

        @Override
        public void onReady() {
            entity.setAI(true);
            JagerA1Info.SOUND.SUMMON_READY.play(getLocation());
        }

        private void onTick(long i) {
            if (!readyTimeModule.isReady())
                return;

            if (i % 10 == 0) {
                if (entity.getTarget() == null) {
                    entity.setAngry(false);
                    entity.setSitting(true);
                    entity.setTamed(true);
                    damageModule.setMaxHealth(JagerA1Info.HEALTH);
                    damageModule.setHealth(getStateValue());

                    Damageable target = CombatUtil.getNearCombatEntity(game, getLocation(), JagerA1Info.ENEMY_DETECT_RADIUS,
                            CombatUtil.EntityCondition.enemy(this).and(Damageable::isCreature));
                    if (target != null) {
                        entity.setTarget(target.getEntity());

                        JagerA1Info.SOUND.ENEMY_DETECT.play(getLocation());
                    }
                } else {
                    entity.setAngry(true);
                    entity.setSitting(false);
                    entity.setTamed(false);
                    damageModule.setMaxHealth(JagerA1Info.HEALTH);
                    damageModule.setHealth(getStateValue());

                    CombatEntity targetCombatEntity = CombatEntity.fromEntity(entity.getTarget());
                    if (targetCombatEntity == null || targetCombatEntity.isDisposed()
                            || (targetCombatEntity instanceof CombatUser && ((CombatUser) targetCombatEntity).isDead()))
                        entity.setTarget(null);
                }
            }
        }

        @Override
        public boolean isCreature() {
            return true;
        }

        @Override
        public double getScore() {
            return JagerA1Info.DEATH_SCORE;
        }

        @Override
        public void onAttack(@NonNull Damageable victim, double damage, boolean isCrit, boolean isUlt) {
            owner.onAttack(victim, damage, isCrit, isUlt);

            combatUser.getSkill(JagerP1Info.getInstance()).setTarget(victim);
            combatUser.useAction(ActionKey.PERIODIC_1);

            if (victim instanceof CombatUser)
                bonusScoreModule.addTarget((CombatUser) victim, JagerA1Info.KILL_SCORE_TIME_LIMIT);
        }

        @Override
        public void onKill(@NonNull Damageable victim) {
            owner.onKill(victim);
        }

        @Override
        public void onDefaultAttack(@NonNull Damageable victim) {
            victim.getDamageModule().damage(this, JagerA1Info.DAMAGE, DamageType.NORMAL, null,
                    victim.getStatusEffectModule().hasType(StatusEffectType.SNARE), true);
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location,
                             boolean isCrit) {
            setStateValue((int) damageModule.getHealth());

            JagerA1Info.SOUND.DAMAGE.play(getLocation(), 1 + damage * 0.001);
            CombatEffectUtil.playBleedingParticle(this, location, damage);
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            dispose();

            setStateValue(0);
            setCooldown(JagerA1Info.COOLDOWN_DEATH);

            JagerA1Info.SOUND.DEATH.play(getLocation());
        }
    }
}
