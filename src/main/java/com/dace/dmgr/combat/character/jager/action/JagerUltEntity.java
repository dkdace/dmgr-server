package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.character.jager.JagerTrait;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.AttackModule;
import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.entity.module.ReadyTimeModule;
import com.dace.dmgr.combat.interaction.FixedPitchHitbox;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.Getter;
import lombok.NonNull;
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
public final class JagerUltEntity extends SummonEntity<MagmaCube> implements HasReadyTime, Damageable, Attacker {
    /** 스킬 객체 */
    private final JagerUlt skill;
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

    public JagerUltEntity(@NonNull MagmaCube entity, @NonNull CombatUser owner) {
        super(
                entity,
                owner.getName() + "의 눈폭풍 발생기",
                owner,
                true,
                new FixedPitchHitbox(entity.getLocation(), 0.7, 0.2, 0.7, 0, 0.1, 0)
        );
        skill = (JagerUlt) owner.getSkill(JagerUltInfo.getInstance());
        attackModule = new AttackModule(this);
        damageModule = new DamageModule(this, false, JagerUltInfo.HEALTH);
        readyTimeModule = new ReadyTimeModule(this, JagerUltInfo.SUMMON_DURATION);

        onInit();
    }

    private void onInit() {
        entity.setAI(false);
        entity.setSize(1);
        entity.setSilent(true);
        entity.setInvulnerable(true);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false, false), true);
        GlowAPI.setGlowing(entity, GlowAPI.Color.WHITE, owner.getEntity());
        SoundUtil.play(Sound.ENTITY_PLAYER_HURT, entity.getLocation(), 0.5, 0.5);

        damageModule.setMaxHealth(JagerUltInfo.HEALTH);
        damageModule.setHealth(JagerUltInfo.HEALTH);
    }

    @Override
    public void activate() {
        super.activate();
        readyTimeModule.ready();
    }

    @NonNull
    public Location[] getPassCheckLocations() {
        return new Location[]{entity.getLocation().add(0, 0.2, 0)};
    }

    @Override
    public void onTickBeforeReady(long i) {
        if (LocationUtil.isNonSolid(entity.getLocation().add(0, 0.2, 0)))
            entity.teleport(entity.getLocation().add(0, 0.2, 0));
        playBeforeReadyEffect();
        playTickEffect();
    }

    /**
     * 준비 대기 시간의 효과를 재생한다.
     */
    private void playBeforeReadyEffect() {
        ParticleUtil.play(Particle.EXPLOSION_NORMAL, entity.getLocation(), 0, 0, -1, 0, 0.3);
        SoundUtil.play(Sound.BLOCK_FIRE_EXTINGUISH, entity.getLocation(), 0.8, 1.7);
    }

    @Override
    public void onReady() {
        // 미사용
    }

    @Override
    protected void onTick(long i) {
        playTickEffect();
        if (!readyTimeModule.isReady())
            return;

        double range = Math.min(JagerUltInfo.MIN_RADIUS + ((double) i / JagerUltInfo.MAX_RADIUS_DURATION) * (JagerUltInfo.MAX_RADIUS - JagerUltInfo.MIN_RADIUS),
                JagerUltInfo.MAX_RADIUS);
        playTickEffect(i, range);

        if (i % 4 == 0) {
            CombatEntity[] targets = CombatUtil.getNearEnemies(this, entity.getLocation(), range,
                    combatEntity -> combatEntity instanceof Damageable &&
                            combatEntity.getEntity().getLocation().add(0, combatEntity.getEntity().getHeight(), 0).getY() < entity.getLocation().getY() &&
                            canPass(combatEntity));
            for (CombatEntity target : targets) {
                onFindEnemy((Damageable) target);
            }
        }
        if (i >= JagerUltInfo.DURATION)
            dispose();
    }

    /**
     * 발생기 표시 효과를 재생한다.
     */
    private void playTickEffect() {
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, entity.getLocation(), 8, 0.6, 0.02, 0.6,
                96, 220, 255);
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, entity.getLocation(), 3, 0.15, 0.02, 0.15,
                80, 80, 100);
    }

    /**
     * 적을 찾았을 때 실행할 작업.
     *
     * @param target 대상 엔티티
     */
    private void onFindEnemy(@NonNull Damageable target) {
        target.getDamageModule().damage(this, JagerUltInfo.DAMAGE_PER_SECOND * 4 / 20, DamageType.ENTITY, false, false);
        JagerTrait.addFreezeValue(target, JagerUltInfo.FREEZE_PER_SECOND * 4 / 20);
    }

    /**
     * 발생기 표시 효과를 재생한다.
     *
     * @param i     인덱스
     * @param range 현재 범위. (단위: 블록)
     */
    private void playTickEffect(long i, double range) {
        long angle = i * 14;
        if (i <= JagerUltInfo.DURATION - 100 && i % 30 == 0) {
            SoundUtil.play(Sound.ITEM_ELYTRA_FLYING, entity.getLocation(), 3, 1.3, 0.2);
            SoundUtil.play(Sound.ITEM_ELYTRA_FLYING, entity.getLocation(), 3, 1.7, 0.2);
        }

        for (int _i = 1; _i <= 6; _i++) {
            Location loc = entity.getLocation();
            loc.setYaw(0);
            loc.setPitch(0);
            Vector vector = VectorUtil.getRollAxis(loc);
            Vector axis = VectorUtil.getYawAxis(loc);

            angle += 19;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);

            double distance = range / 6 * _i;
            double offsetX = vec.getX();
            double offsetZ = vec.getZ();

            ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc.clone().add(vec.clone().multiply(distance)), 0,
                    offsetX, -0.6, offsetZ, 0.05 * (7 - _i));
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc.clone().subtract(vec.clone().multiply(distance)), 0,
                    offsetX, 0.6, offsetZ, -0.05 * (7 - _i));
            ParticleUtil.play(Particle.SNOW_SHOVEL, loc.clone().add(vec.clone().multiply(distance)).subtract(0, 2.5, 0),
                    5, 0, 1.4, 0, 0.04);
            ParticleUtil.play(Particle.SNOW_SHOVEL, loc.clone().subtract(vec.clone().multiply(distance)).subtract(0, 2.5, 0),
                    5, 0, 1.4, 0, 0.04);
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
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0, entity.getLocation(), 120,
                0.1F, 0.1F, 0.1F, 0.15F);
        ParticleUtil.play(Particle.CRIT, entity.getLocation(), 80, 0.1F, 0.1F, 0.1F, 0.5F);
        ParticleUtil.play(Particle.EXPLOSION_LARGE, entity.getLocation(), 1, 0, 0, 0, 0);
        SoundUtil.play(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, entity.getLocation(), 2F, 0.7F);
        SoundUtil.play(Sound.ENTITY_ITEM_BREAK, entity.getLocation(), 2F, 0.7F);
        SoundUtil.play(Sound.ENTITY_GENERIC_EXPLODE, entity.getLocation(), 2F, 1.2F);
    }
}
