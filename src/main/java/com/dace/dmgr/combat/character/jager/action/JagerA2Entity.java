package com.dace.dmgr.combat.character.jager.action;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Hitbox;
import com.dace.dmgr.combat.entity.SummonEntity;
import com.dace.dmgr.combat.entity.statuseffect.Snare;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.*;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.inventivetalent.glow.GlowAPI;

/**
 * 예거 - 곰덫 클래스.
 */
public final class JagerA2Entity extends SummonEntity<MagmaCube> {
    /** 스킬 객체 */
    private final JagerA2 skill;

    public JagerA2Entity(MagmaCube entity, CombatUser owner) {
        super(
                entity,
                "§f" + owner.getName() + "의 곰덫",
                true,
                JagerA2Info.HEALTH,
                owner,
                new FixedPitchHitbox(entity.getLocation(), 0.8, 0.1, 0.8, 0, 0.05, 0)
        );
        skill = (JagerA2) owner.getSkill(JagerA2Info.getInstance());
    }

    @Override
    public void onTick(int i) {
        super.onTick(i);

        if (i < JagerA2Info.SUMMON_DURATION)
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, entity.getLocation(), 5, 0.2F, 0.2F, 0.2F, 120, 120, 135);
        else if (i == JagerA2Info.SUMMON_DURATION)
            playReadySound();
        else if (i > JagerA2Info.SUMMON_DURATION) {
            CombatEntity<?> target = CombatUtil.getNearEnemy(this, entity.getLocation(), 0.8F,
                    combatEntity -> !combatEntity.isFixed());
            if (target != null)
                onCatchEnemy(target);
        }

        playTickEffect();
    }

    /**
     * {@link JagerA2Entity#onTick(int)}에서 덫 표시 효과를 재생한다.
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
     * {@link JagerA2Entity#onTick(int)}에서 덫 발동 시 실행할 작업.
     *
     * @param target 대상 엔티티
     */
    private void onCatchEnemy(CombatEntity<?> target) {
        playCatchSound();
        target.damage(this, JagerA2Info.DAMAGE, "", false, true);
        target.applyStatusEffect(new Snare(), JagerA2Info.SNARE_DURATION);

        remove();
        skill.setSummonEntity(null);
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
    public void onAttack(CombatEntity<?> victim, int damage, String type, boolean isCrit, boolean isUlt) {
        JagerA1 skill1 = (JagerA1) owner.getSkill(JagerA1Info.getInstance());

        if (!skill1.isDurationFinished() && skill1.getSummonEntity().getEntity().getTarget() == null)
            skill1.getSummonEntity().getEntity().setTarget(victim.getEntity());
    }

    @Override
    public void onDamage(CombatEntity<?> attacker, int damage, String type, boolean isCrit, boolean isUlt) {
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
    public void onDeath(CombatEntity<?> attacker) {
        super.onDeath(attacker);

        playDeathEffect();
        skill.setSummonEntity(null);
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

    @Override
    protected void onInitTemporalEntity(Location location) {
        setTeam(owner.getTeam());
        entity.setAI(false);
        entity.setSize(1);
        entity.setSilent(true);
        entity.setInvulnerable(true);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false, false), true);
        entity.teleport(location.add(0, 0.2, 0));
        setMaxHealth(JagerA2Info.HEALTH);
        setHealth(JagerA2Info.HEALTH);
        GlowAPI.setGlowing(entity, GlowAPI.Color.WHITE, owner.getEntity());

        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(new int[]{entity.getEntityId()});
        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            CombatUser combatUser2 = EntityInfoRegistry.getCombatUser(player2);
            if (combatUser2 != null && CombatUtil.isEnemy(owner, combatUser2))
                packet.sendPacket(player2);
        });
    }

    /**
     * 준비 시 효과음을 재생한다.
     */
    private void playReadySound() {
        SoundUtil.play(Sound.ENTITY_PLAYER_HURT, entity.getLocation(), 0.5F, 0.5F);
    }
}
