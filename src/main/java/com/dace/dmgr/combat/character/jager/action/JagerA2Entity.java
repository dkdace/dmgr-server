package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.*;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.FixedPitchHitbox;
import com.dace.dmgr.util.GlowUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
        playInitSound();

        damageModule.setMaxHealth(JagerA2Info.HEALTH);
        damageModule.setHealth(JagerA2Info.HEALTH);
    }

    /**
     * 소환 시 효과음을 재생한다.
     */
    private void playInitSound() {
        SoundUtil.play(Sound.ENTITY_HORSE_ARMOR, entity.getLocation(), 0.5, 1.6);
        SoundUtil.play("random.craft", entity.getLocation(), 0.5, 1.3);
        SoundUtil.play(Sound.ENTITY_PLAYER_HURT, entity.getLocation(), 0.5, 0.5);
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
        SoundUtil.play(Sound.ENTITY_PLAYER_HURT, entity.getLocation(), 0.5, 0.5);
    }

    @Override
    protected void onTick(long i) {
        if (!readyTimeModule.isReady())
            return;

        Damageable target = (Damageable) CombatUtil.getNearCombatEntity(game, entity.getLocation(), 0.8,
                combatEntity -> combatEntity instanceof Damageable && combatEntity.isEnemy(this));
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
        playCatchSound();
        target.getDamageModule().damage(this, JagerA2Info.DAMAGE, DamageType.NORMAL, false, true);
        target.getStatusEffectModule().applyStatusEffect(StatusEffectType.SNARE, JagerA2Info.SNARE_DURATION);

        dispose();
    }

    /**
     * 덫 발동 시 효과음을 재생한다.
     */
    private void playCatchSound() {
        SoundUtil.play(Sound.ENTITY_SHEEP_SHEAR, entity.getLocation(), 2, 1.2);
        SoundUtil.play("new.entity.player.hurt_sweet_berry_bush", entity.getLocation(), 2, 0.8);
        SoundUtil.play("random.metalhit", entity.getLocation(), 2, 1.2);
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
    public void onDamage(Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, boolean isCrit, boolean isUlt) {
        SoundUtil.play("random.metalhit", entity.getLocation(), 0.4 + damage * 0.001, 1.1, 0.1);
    }

    @Override
    public void onDeath(Attacker attacker) {
        dispose();
        playDeathEffect();
    }

    /**
     * 죽었을 때 효과를 재생한다.
     */
    private void playDeathEffect() {
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0, entity.getLocation(), 80,
                0.1, 0.1, 0.1, 0.15);
        SoundUtil.play(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, entity.getLocation(), 1, 0.8);
        SoundUtil.play("random.metalhit", entity.getLocation(), 1, 0.8);
        SoundUtil.play(Sound.ENTITY_ITEM_BREAK, entity.getLocation(), 1, 0.8);
    }
}
