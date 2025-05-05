package com.dace.dmgr.combat.entity.temporary.dummy;

import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.Sound;

/**
 * 더미가 사용하는 투사체 총알 클래스.
 */
final class DummyProjectile extends Projectile<Damageable> {
    /** 적 총알 궤적 입자 효과 */
    private static final ParticleEffect ENEMY_BULLET_TRAIL_PARTICLE = new ParticleEffect(
            ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                    Color.fromRGB(255, 60, 60)).count(3).horizontalSpread(0.05).verticalSpread(0.05).build());
    /** 아군 총알 궤적 입자 효과 */
    private static final ParticleEffect TEAM_BULLET_TRAIL_PARTICLE = new ParticleEffect(
            ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                    Color.fromRGB(60, 255, 60)).count(3).horizontalSpread(0.05).verticalSpread(0.05).build());
    /** 발사 효과음 */
    private static final SoundEffect SHOOT_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_SHULKER_SHOOT).volume(0.8).pitch(1.3).build());

    /** 피해량 */
    private final int damage;

    /**
     * 더미 투사체 총알을 생성한다.
     *
     * @param shooter 발사자
     * @param damage  피해량
     */
    DummyProjectile(@NonNull Dummy shooter, int damage) {
        super(shooter, 30, EntityCondition.enemy(shooter));

        this.damage = damage;
        SHOOT_SOUND.play(shooter.getLocation());
    }

    @Override
    @NonNull
    protected IntervalHandler getIntervalHandler() {
        return createPeriodIntervalHandler(10, (((Dummy) shooter).isEnemy() ? ENEMY_BULLET_TRAIL_PARTICLE : TEAM_BULLET_TRAIL_PARTICLE)::play);
    }

    @Override
    @NonNull
    protected HitBlockHandler getHitBlockHandler() {
        return (location, hitBlock) -> false;
    }

    @Override
    @NonNull
    protected HitEntityHandler<Damageable> getHitEntityHandler() {
        return (location, target) -> {
            target.getDamageModule().damage(this, damage, DamageType.NORMAL, location, false, false);
            return false;
        };
    }
}
