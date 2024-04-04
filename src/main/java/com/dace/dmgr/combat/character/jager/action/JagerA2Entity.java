package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.*;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.FixedPitchHitbox;
import com.dace.dmgr.util.GlowUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.MagmaCube;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 예거 - 곰덫 클래스.
 */
public final class JagerA2Entity extends SummonEntity<MagmaCube> implements HasReadyTime, Damageable, Attacker {
    /** 스킬 객체 */
    private final JagerA2 skill;
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
    /** 준비 시간 모듈 */
    @NonNull
    @Getter
    private final ReadyTimeModule readyTimeModule;

    public JagerA2Entity(@NonNull MagmaCube entity, @NonNull CombatUser owner) {
        super(
                entity,
                owner.getName() + "의 곰덫",
                owner,
                true,
                new FixedPitchHitbox(entity.getLocation(), 0.8, 0.1, 0.8, 0, 0.05, 0)
        );
        skill = (JagerA2) owner.getSkill(JagerA2Info.getInstance());
        knockbackModule = new KnockbackModule(this, 1);
        statusEffectModule = new StatusEffectModule(this, 1);
        attackModule = new AttackModule(this);
        damageModule = new DamageModule(this, false, JagerA2Info.HEALTH);
        readyTimeModule = new ReadyTimeModule(this, JagerA2Info.SUMMON_DURATION);

        onInit();
    }

    private void onInit() {
        entity.setAI(false);
        entity.setSize(1);
        entity.setSilent(true);
        entity.setInvulnerable(true);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false, false), true);
        entity.teleport(entity.getLocation().add(0, 0.05, 0));
        GlowUtil.setGlowing(entity, ChatColor.WHITE, owner.getEntity());
        SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A2_SUMMON, entity.getLocation());

        damageModule.setMaxHealth(JagerA2Info.HEALTH);
        damageModule.setHealth(JagerA2Info.HEALTH);
    }

    @Override
    public void activate() {
        super.activate();
        readyTimeModule.ready();
    }

    @Override
    public void onTickBeforeReady(long i) {
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, entity.getLocation(), 5, 0.2, 0.2, 0.2,
                120, 120, 135);
        playTickEffect();
    }

    @Override
    public void onReady() {
        SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A2_SUMMON_READY, entity.getLocation());
    }

    @Override
    protected void onTick(long i) {
        if (!readyTimeModule.isReady())
            return;

        Damageable target = (Damageable) CombatUtil.getNearCombatEntity(game, entity.getLocation(), 0.8,
                combatEntity -> combatEntity instanceof Damageable && combatEntity instanceof Living && combatEntity.isEnemy(this));
        if (target != null)
            onCatchEnemy(target);

        playTickEffect();
    }

    /**
     * 덫 표시 효과를 재생한다.
     */
    private void playTickEffect() {
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.4, 0, 0.6), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.55, 0, 0.4), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.4, 0, 0.2), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.55, 0, 0), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.4, 0, -0.2), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.55, 0, -0.4), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.4, 0, -0.6), 1, 0, 0, 0, 0);

        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.4, 0, 0.6), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.55, 0, 0.4), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.4, 0, 0.2), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.55, 0, 0), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.4, 0, -0.2), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.55, 0, -0.4), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.4, 0, -0.6), 1, 0, 0, 0, 0);

        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0, 0, 0.4), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0, 0, 0.2), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0, 0, 0), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0, 0, -0.2), 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0, 0, -0.4), 1, 0, 0, 0, 0);
    }

    /**
     * 덫 발동 시 실행할 작업.
     *
     * @param target 대상 엔티티
     */
    private void onCatchEnemy(@NonNull Damageable target) {
        target.getDamageModule().damage(this, JagerA2Info.DAMAGE, DamageType.NORMAL, target.getEntity().getLocation().add(0, 0.2, 0),
                false, true);
        target.getStatusEffectModule().applyStatusEffect(StatusEffectType.SNARE, JagerA2Info.SNARE_DURATION);
        SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A2_TRIGGER, entity.getLocation());

        dispose();
    }

    @Override
    public void dispose() {
        super.dispose();

        skill.entity = null;
    }

    @Override
    public void onAttack(@NonNull Damageable victim, int damage, @NonNull DamageType damageType, boolean isCrit, boolean isUlt) {
        owner.onAttack(victim, damage, damageType, isCrit, isUlt);
        JagerA1 skill1 = (JagerA1) owner.getSkill(JagerA1Info.getInstance());

        if (!skill1.isDurationFinished() && skill1.entity.getEntity().getTarget() == null)
            skill1.entity.getEntity().setTarget(victim.getEntity());
    }

    @Override
    public void onKill(@NonNull Damageable victim) {
        owner.onKill(victim);
    }

    @Override
    public void onDamage(Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, Location location, boolean isCrit, boolean isUlt) {
        SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A2_DAMAGE, entity.getLocation(), 1 + damage * 0.001);
        ParticleUtil.playBreakEffect(location, entity, damage);
    }

    @Override
    public void onDeath(Attacker attacker) {
        dispose();

        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0, entity.getLocation(), 80,
                0.1, 0.1, 0.1, 0.15);
        SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A2_DEATH, entity.getLocation());
    }
}
