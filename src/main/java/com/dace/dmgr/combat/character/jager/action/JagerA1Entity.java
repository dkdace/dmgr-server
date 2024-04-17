package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.*;
import com.dace.dmgr.combat.entity.module.StatusEffectModule;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.temporal.SummonEntity;
import com.dace.dmgr.combat.interaction.FixedPitchHitbox;
import com.dace.dmgr.util.GlowUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Wolf;
import org.jetbrains.annotations.Nullable;

/**
 * 예거 - 설랑 클래스.
 */
public final class JagerA1Entity extends SummonEntity<Wolf> implements HasReadyTime, Damageable, Attacker, Living, Jumpable {
    /** 스킬 객체 */
    private final JagerA1 skill;
    /** 넉백 모듈 */
    @NonNull
    @Getter
    private final KnockbackModule knockbackModule;
    /** 상태 효과 모듈 */
    @NonNull
    @Getter
    private final StatusEffectModule statusEffectModule;
    /** 공격 모듈 */
    @NonNull
    @Getter
    private final AttackModule attackModule;
    /** 피해 모듈 */
    @NonNull
    @Getter
    private final DamageModule damageModule;
    /** 이동 모듈 */
    @NonNull
    @Getter
    private final JumpModule moveModule;
    /** 준비 대기시간 모듈 */
    @NonNull
    @Getter
    private final ReadyTimeModule readyTimeModule;

    public JagerA1Entity(@NonNull Wolf entity, @NonNull CombatUser owner) {
        super(
                entity,
                owner.getName() + "의 설랑",
                owner,
                false,
                new FixedPitchHitbox(entity.getLocation(), 0.4, 0.8, 1.2, 0, 0.4, 0)
        );
        skill = (JagerA1) owner.getSkill(JagerA1Info.getInstance());
        knockbackModule = new KnockbackModule(this);
        statusEffectModule = new StatusEffectModule(this);
        attackModule = new AttackModule(this);
        damageModule = new DamageModule(this, false, true, JagerA1Info.HEALTH);
        moveModule = new JumpModule(this, entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * 1.5);
        readyTimeModule = new ReadyTimeModule(this, JagerA1Info.SUMMON_DURATION);

        onInit();
    }

    private void onInit() {
        entity.setAI(false);
        entity.setCollarColor(DyeColor.CYAN);
        entity.setTamed(true);
        entity.setOwner(owner.getEntity());
        entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
        GlowUtil.setGlowing(entity, ChatColor.WHITE, owner.getEntity());
        SoundUtil.playNamedSound(NamedSound.COMBAT_ENTITY_SUMMON, entity.getLocation());

        damageModule.setHealth((int) skill.getStateValue());
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
        SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A1_SUMMON_READY, entity.getLocation());
        entity.setAI(true);
    }

    @Override
    protected void onTick(long i) {
        if (!readyTimeModule.isReady())
            return;

        if (i % 10 == 0 && entity.getTarget() == null) {
            Damageable target = (Damageable) CombatUtil.getNearCombatEntity(game, entity.getLocation(), JagerA1Info.LOW_HEALTH_DETECT_RADIUS,
                    combatEntity -> combatEntity instanceof Damageable && combatEntity.isEnemy(this) &&
                            ((Damageable) combatEntity).getDamageModule().isLowHealth());
            if (target != null)
                entity.setTarget(target.getEntity());
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        skill.entity = null;
    }

    @Override
    public void onAttack(@NonNull Damageable victim, int damage, @NonNull DamageType damageType, boolean isCrit, boolean isUlt) {
        owner.onAttack(victim, damage, damageType, isCrit, isUlt);
    }

    @Override
    public void onKill(@NonNull Damageable victim) {
        owner.onKill(victim);
    }

    @Override
    public void onDefaultAttack(@NonNull Damageable victim) {
        victim.getDamageModule().damage(this, JagerA1Info.DAMAGE, DamageType.NORMAL, null,
                victim.getStatusEffectModule().hasStatusEffect(StatusEffectType.SNARE), true);
    }

    @Override
    public void onDamage(@Nullable Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, @Nullable Location location, boolean isCrit, boolean isUlt) {
        SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A1_DAMAGE, entity.getLocation(), 1 + damage * 0.001);
        CombatUtil.playBleedingEffect(location, entity, damage);
        skill.addStateValue(-damage);
    }

    @Override
    public void onDeath(@Nullable Attacker attacker) {
        dispose();

        SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A1_DEATH, entity.getLocation());
        skill.setStateValue(0);
        skill.setCooldown(JagerA1Info.COOLDOWN_DEATH);
    }
}
