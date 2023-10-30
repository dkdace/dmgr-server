package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.CombatEntityModule;
import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.entity.statuseffect.Snare;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.MagmaCube;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.inventivetalent.glow.GlowAPI;

/**
 * 예거 - 곰덫 클래스.
 */
public final class JagerA2Entity extends SummonEntity<MagmaCube> implements Damageable, Attacker {
    /** 스킬 객체 */
    private final JagerA2 skill;
    /** 피해 모듈 */
    @Getter
    private final DamageModule damageModule;

    public JagerA2Entity(MagmaCube entity, CombatUser owner) {
        super(
                entity,
                "§f" + owner.getName() + "의 곰덫",
                owner,
                new FixedPitchHitbox(entity.getLocation(), 0.8, 0.1, 0.8, 0, 0.05, 0)
        );
        skill = (JagerA2) owner.getSkill(JagerA2Info.getInstance());
        damageModule = new DamageModule(this, false, JagerA2Info.HEALTH);
    }

    @Override
    protected CombatEntityModule[] getModules() {
        return new CombatEntityModule[]{damageModule};
    }

    @Override
    public void init() {
        super.init();

        entity.setAI(false);
        entity.setSize(1);
        entity.setSilent(true);
        entity.setInvulnerable(true);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false, false), true);
        entity.teleport(entity.getLocation().add(0, 0.05, 0));
        damageModule.setMaxHealth(JagerA2Info.HEALTH);
        damageModule.setHealth(JagerA2Info.HEALTH);
        GlowAPI.setGlowing(entity, GlowAPI.Color.WHITE, owner.getEntity());
        hideForEnemies();
        playInitSound();
    }

    /**
     * 소환 시 효과음을 재생한다.
     */
    private void playInitSound() {
        SoundUtil.play(Sound.ENTITY_HORSE_ARMOR, entity.getLocation(), 0.5F, 1.6F);
        SoundUtil.play("random.craft", entity.getLocation(), 0.5F, 1.3F);
        SoundUtil.play(Sound.ENTITY_PLAYER_HURT, entity.getLocation(), 0.5F, 0.5F);
    }

    @Override
    public void onTick(int i) {
        super.onTick(i);

        if (i < JagerA2Info.SUMMON_DURATION)
            playSummonEffect();
        else if (i == JagerA2Info.SUMMON_DURATION)
            playReadySound();
        else if (i > JagerA2Info.SUMMON_DURATION) {
            Damageable target = (Damageable) CombatUtil.getNearEnemy(this, entity.getLocation(), 0.8F,
                    combatEntity -> combatEntity instanceof Damageable && canPass(combatEntity));
            if (target != null)
                onCatchEnemy(target);
        }

        playTickEffect();
    }

    /**
     * 준비 대기 시간의 효과를 재생한다.
     */
    private void playSummonEffect() {
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, entity.getLocation(), 5, 0.2F, 0.2F, 0.2F, 120, 120, 135);
    }

    /**
     * 준비 시 효과음을 재생한다.
     */
    private void playReadySound() {
        SoundUtil.play(Sound.ENTITY_PLAYER_HURT, entity.getLocation(), 0.5F, 0.5F);
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
    private void onCatchEnemy(Damageable target) {
        playCatchSound();
        target.getDamageModule().damage(this, JagerA2Info.DAMAGE, DamageType.ENTITY, false, true);
        target.applyStatusEffect(new Snare(), JagerA2Info.SNARE_DURATION);

        remove();
    }

    /**
     * 덫 발동 시 효과음을 재생한다.
     */
    private void playCatchSound() {
        SoundUtil.play(Sound.ENTITY_SHEEP_SHEAR, entity.getLocation(), 2F, 1.2F);
        SoundUtil.play("new.entity.player.hurt_sweet_berry_bush", entity.getLocation(), 2F, 0.8F);
        SoundUtil.play("random.metalhit", entity.getLocation(), 2F, 1.2F);
    }

    @Override
    public void remove() {
        super.remove();

        skill.getHasEntityModule().setSummonEntity(null);
    }

    @Override
    public void onAttack(Damageable victim, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
        owner.onAttack(victim, damage, damageType, isCrit, isUlt);
        JagerA1 skill1 = (JagerA1) owner.getSkill(JagerA1Info.getInstance());

        if (!skill1.isDurationFinished() && skill1.getHasEntityModule().getSummonEntity().getEntity().getTarget() == null)
            skill1.getHasEntityModule().getSummonEntity().getEntity().setTarget(victim.getEntity());
    }

    @Override
    public void onKill(Damageable victim) {
        owner.onKill(victim);
    }

    @Override
    public void onDamage(Attacker attacker, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
        playDamageSound(damage);
    }

    /**
     * 피해를 입었을 때 효과음을 재생한다.
     *
     * @param damage 피해량
     */
    private void playDamageSound(float damage) {
        SoundUtil.play("random.metalhit", entity.getLocation(), (float) (0.4 + damage * 0.001), (float) (1.1 + Math.random() * 0.1));
    }

    @Override
    public void onDeath(Attacker attacker) {
        remove();
        playDeathEffect();
    }

    /**
     * 죽었을 때 효과를 재생한다.
     */
    private void playDeathEffect() {
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0, entity.getLocation(), 80,
                0.1F, 0.1F, 0.1F, 0.15F);
        SoundUtil.play(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, entity.getLocation(), 1F, 0.8F);
        SoundUtil.play("random.metalhit", entity.getLocation(), 1F, 0.8F);
        SoundUtil.play(Sound.ENTITY_ITEM_BREAK, entity.getLocation(), 1F, 0.8F);
    }
}
