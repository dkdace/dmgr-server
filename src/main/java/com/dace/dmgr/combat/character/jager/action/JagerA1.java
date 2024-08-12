package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.action.skill.Confirmable;
import com.dace.dmgr.combat.action.skill.module.LocationConfirmModule;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.*;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.FixedPitchHitbox;
import com.dace.dmgr.util.*;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Wolf;
import org.jetbrains.annotations.Nullable;

public final class JagerA1 extends ChargeableSkill implements Confirmable {
    /** 처치 점수 제한시간 쿨타임 ID */
    public static final String KILL_SCORE_COOLDOWN_ID = "JagerA1KillScoreTimeLimit";

    /** 위치 확인 모듈 */
    @NonNull
    @Getter
    private final LocationConfirmModule confirmModule;
    /** 소환한 엔티티 */
    @Nullable
    private JagerA1Entity summonEntity = null;

    public JagerA1(@NonNull CombatUser combatUser) {
        super(combatUser, JagerA1Info.getInstance(), 0);
        confirmModule = new LocationConfirmModule(this, ActionKey.LEFT_CLICK, ActionKey.SLOT_1, JagerA1Info.SUMMON_MAX_DISTANCE);
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
    public long getDefaultCooldown() {
        return JagerA1Info.COOLDOWN;
    }

    @Override
    public int getMaxStateValue() {
        return JagerA1Info.HEALTH;
    }

    @Override
    public int getStateValueDecrement() {
        return 0;
    }

    @Override
    public int getStateValueIncrement() {
        return JagerA1Info.HEALTH / JagerA1Info.RECOVER_DURATION;
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
                    setDuration(0);
                    if (summonEntity != null)
                        summonEntity.dispose();
                }

                break;
            }
            case LEFT_CLICK: {
                onAccept();

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

        confirmModule.setChecking(false);
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

    @Override
    public void onAccept() {
        if (!confirmModule.isValid())
            return;

        setDuration();
        confirmModule.toggleCheck();
        combatUser.getWeapon().setCooldown(2);

        Wolf wolf = CombatUtil.spawnEntity(Wolf.class, confirmModule.getCurrentLocation());
        summonEntity = new JagerA1Entity(wolf, combatUser);
        summonEntity.activate();
    }

    @Override
    public void reset() {
        super.reset();

        if (summonEntity != null)
            summonEntity.dispose();
    }

    /**
     * 설랑 클래스.
     */
    @Getter
    public final class JagerA1Entity extends SummonEntity<Wolf> implements HasReadyTime, Damageable, Attacker, Jumpable, CombatEntity {
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
        /** 이동 모듈 */
        @NonNull
        private final JumpModule moveModule;
        /** 준비 대기시간 모듈 */
        @NonNull
        private final ReadyTimeModule readyTimeModule;

        private JagerA1Entity(@NonNull Wolf entity, @NonNull CombatUser owner) {
            super(
                    entity,
                    owner.getName() + "의 설랑",
                    owner,
                    true, false,
                    new FixedPitchHitbox(entity.getLocation(), 0.4, 0.8, 1.2, 0, 0.4, 0)
            );
            knockbackModule = new KnockbackModule(this);
            statusEffectModule = new StatusEffectModule(this);
            attackModule = new AttackModule(this);
            damageModule = new DamageModule(this, false, true, true, JagerA1Info.DEATH_SCORE, JagerA1Info.HEALTH);
            moveModule = new JumpModule(this, JagerA1Info.SPEED);
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

            GlowUtil.setGlowing(entity, ChatColor.WHITE, owner.getEntity());
            SoundUtil.playNamedSound(NamedSound.COMBAT_ENTITY_SUMMON, entity.getLocation());
        }

        @Override
        public void activate() {
            super.activate();
            readyTimeModule.ready();
        }

        @Override
        public void onTickBeforeReady(long i) {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, entity.getLocation(), 5, 0.2, 0.2, 0.2,
                    255, 255, 255);
        }

        @Override
        public void onReady() {
            entity.setAI(true);
            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A1_SUMMON_READY, entity.getLocation());
        }

        @Override
        protected void onTick(long i) {
            if (!readyTimeModule.isReady())
                return;

            if (i % 10 == 0) {
                if (entity.getTarget() == null) {
                    entity.setAngry(false);
                    entity.setSitting(true);
                    entity.setTamed(true);

                    Damageable target = (Damageable) CombatUtil.getNearCombatEntity(game, entity.getLocation(), JagerA1Info.ENEMY_DETECT_RADIUS,
                            combatEntity -> combatEntity instanceof Damageable && ((Damageable) combatEntity).getDamageModule().isLiving() &&
                                    combatEntity.isEnemy(this));
                    if (target != null) {
                        entity.setTarget(target.getEntity());
                        SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A1_ENEMY_DETECT, entity.getLocation());
                    }
                } else {
                    entity.setAngry(true);
                    entity.setSitting(false);
                    entity.setTamed(false);

                    CombatEntity targetCombatEntity = CombatEntity.fromEntity(entity.getTarget());
                    if (targetCombatEntity == null || targetCombatEntity.isDisposed() ||
                            (targetCombatEntity instanceof CombatUser && ((CombatUser) targetCombatEntity).isDead()))
                        entity.setTarget(null);
                }
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
            CooldownUtil.setCooldown(combatUser, KILL_SCORE_COOLDOWN_ID + victim, JagerA1Info.KILL_SCORE_TIME_LIMIT);

            JagerP1 skillp1 = combatUser.getSkill(JagerP1Info.getInstance());
            skillp1.setTarget(victim);
            combatUser.useAction(ActionKey.PERIODIC_1);
        }

        @Override
        public void onKill(@NonNull Damageable victim) {
            owner.onKill(victim);
        }

        @Override
        public void onDefaultAttack(@NonNull Damageable victim) {
            victim.getDamageModule().damage(this, JagerA1Info.DAMAGE, DamageType.NORMAL, null,
                    victim.getStatusEffectModule().hasStatusEffectType(StatusEffectType.SNARE), true);
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, @Nullable Location location,
                             boolean isCrit, boolean isUlt) {
            addStateValue(-damage);

            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A1_DAMAGE, entity.getLocation(), 1 + damage * 0.001);
            CombatEffectUtil.playBleedingEffect(location, entity, damage);
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            dispose();

            setStateValue(0);
            setCooldown(JagerA1Info.COOLDOWN_DEATH);

            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A1_DEATH, entity.getLocation());
        }
    }
}
