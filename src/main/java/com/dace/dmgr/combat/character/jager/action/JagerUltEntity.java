package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.character.jager.JagerTrait;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.damageable.Damageable;
import com.dace.dmgr.combat.entity.temporal.Summonable;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.MagmaCube;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.inventivetalent.glow.GlowAPI;

/**
 * 예거 - 눈폭풍 발생기 클래스.
 */
public final class JagerUltEntity extends CombatEntityBase<MagmaCube> implements Damageable, Attacker, Summonable {
    /** 엔티티를 소환한 플레이어 */
    @Getter
    private final CombatUser owner;
    /** 스킬 객체 */
    private final JagerUlt skill;

    public JagerUltEntity(MagmaCube entity, CombatUser owner) {
        super(
                entity,
                "§f" + owner.getName() + "의 눈폭풍 발생기",
                new FixedPitchHitbox(entity.getLocation(), 0.7, 0.2, 0.7, 0, 0.1, 0)
        );
        skill = (JagerUlt) owner.getSkill(JagerUltInfo.getInstance());
        this.owner = owner;
    }

    @Override
    public void onInit() {
        Damageable.super.onInit();
        Summonable.super.onInit();
    }

    @Override
    public void onInitDamageable() {
    }

    @Override
    public void onInitTemporal() {
        setTeam(owner.getTeam());
        entity.setAI(false);
        entity.setSize(1);
        entity.setSilent(true);
        entity.setInvulnerable(true);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false, false), true);
        GlowAPI.setGlowing(entity, GlowAPI.Color.WHITE, owner.getEntity());
        hideForEnemies();
        playInitSound();
    }

    /**
     * 소환 시 효과음을 재생한다.
     */
    private void playInitSound() {
        SoundUtil.play(Sound.ENTITY_PLAYER_HURT, entity.getLocation(), 0.5F, 0.5F);
    }

    @Override
    public void onTick(int i) {
        if (i < JagerUltInfo.SUMMON_DURATION) {
            if (LocationUtil.isNonSolid(entity.getLocation().add(0, 0.2, 0)))
                entity.teleport(entity.getLocation().add(0, 0.2, 0));
            playSummonEffect();
        } else if (i >= JagerUltInfo.SUMMON_DURATION) {
            float range = Math.min(JagerUltInfo.MIN_RADIUS + ((float) i / JagerUltInfo.MAX_RADIUS_DURATION) * (JagerUltInfo.MAX_RADIUS - JagerUltInfo.MIN_RADIUS),
                    JagerUltInfo.MAX_RADIUS);
            playTickEffect(i, range);

            if (i % 4 == 0) {
                CombatEntity[] targets = CombatUtil.getNearEnemies(this, entity.getLocation(), range,
                        combatEntity -> combatEntity instanceof Damageable && combatEntity.getEntity().getEyeLocation().getY() < entity.getLocation().getY() &&
                                canPass(combatEntity));
                for (CombatEntity target : targets) {
                    onFindEnemy((Damageable) target);
                }
            }
            if (i >= JagerUltInfo.DURATION)
                remove();
        }

        playTickEffect();
    }

    /**
     * 준비 대기 시간의 효과를 재생한다.
     */
    private void playSummonEffect() {
        ParticleUtil.play(Particle.EXPLOSION_NORMAL, entity.getLocation(), 0, 0, -1, 0, 0.3F);
        SoundUtil.play(Sound.BLOCK_FIRE_EXTINGUISH, entity.getLocation(), 0.8F, 1.7F);
    }

    /**
     * 적을 찾았을 때 실행할 작업.
     *
     * @param target 대상 엔티티
     */
    private void onFindEnemy(Damageable target) {
        target.damage(this, JagerUltInfo.DAMAGE_PER_SECOND * 4 / 20, DamageType.ENTITY, false, false);
        JagerTrait.addFreezeValue(target, JagerUltInfo.FREEZE_PER_SECOND * 4 / 20);
    }

    /**
     * 발생기 표시 효과를 재생한다.
     */
    private void playTickEffect() {
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, entity.getLocation(), 8, 0.6F, 0.02F, 0.6F, 96, 220, 255);
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, entity.getLocation(), 3, 0.15F, 0.02F, 0.15F, 80, 80, 100);
    }

    /**
     * 발생기 표시 효과를 재생한다.
     *
     * @param i     인덱스
     * @param range 현재 범위
     */
    private void playTickEffect(int i, float range) {
        int j = (int) (i - JagerUltInfo.SUMMON_DURATION);

        int angle = j * 14;
        if (j <= JagerUltInfo.DURATION - 100 && j % 30 == 0) {
            SoundUtil.play(Sound.ITEM_ELYTRA_FLYING, entity.getLocation(), 3F, (float) (1.2 + Math.random() * 0.2));
            SoundUtil.play(Sound.ITEM_ELYTRA_FLYING, entity.getLocation(), 3F, (float) (1.7 + Math.random() * 0.2));
        }

        for (int _i = 1; _i <= 6; _i++) {
            Location loc = entity.getLocation();
            loc.setYaw(0);
            loc.setPitch(0);
            Vector vector = VectorUtil.getRollAxis(loc);
            Vector axis = VectorUtil.getYawAxis(loc);

            angle += 19;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);

            float distance = range / 6 * _i;
            float offsetX = (float) (vec.getX());
            float offsetZ = (float) (vec.getZ());

            ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc.clone().add(vec.clone().multiply(distance)), 0,
                    offsetX, -0.6F, offsetZ, 0.05F * (7 - _i));
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc.clone().subtract(vec.clone().multiply(distance)), 0,
                    offsetX, 0.6F, offsetZ, -0.05F * (7 - _i));
            ParticleUtil.play(Particle.SNOW_SHOVEL, loc.clone().add(vec.clone().multiply(distance)).subtract(0, 2.5, 0),
                    5, 0, 1.4F, 0, 0.04F);
            ParticleUtil.play(Particle.SNOW_SHOVEL, loc.clone().subtract(vec.clone().multiply(distance)).subtract(0, 2.5, 0),
                    5, 0, 1.4F, 0, 0.04F);
        }
    }

    @Override
    public void onRemoveTemporal() {
        skill.setSummonEntity(null);
    }

    @Override
    public int getMaxHealth() {
        return JagerUltInfo.HEALTH;
    }

    @Override
    public void onAttack(Damageable victim, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
        owner.onAttack(victim, damage, damageType, isCrit, isUlt);
    }

    @Override
    public void onKill(CombatEntity victim) {
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
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0, entity.getLocation(), 120,
                0.1F, 0.1F, 0.1F, 0.15F);
        ParticleUtil.play(Particle.CRIT, entity.getLocation(), 80, 0.1F, 0.1F, 0.1F, 0.5F);
        ParticleUtil.play(Particle.EXPLOSION_LARGE, entity.getLocation(), 1, 0, 0, 0, 0);
        SoundUtil.play(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, entity.getLocation(), 2F, 0.7F);
        SoundUtil.play(Sound.ENTITY_ITEM_BREAK, entity.getLocation(), 2F, 0.7F);
        SoundUtil.play(Sound.ENTITY_GENERIC_EXPLODE, entity.getLocation(), 2F, 1.2F);
    }
}
