package com.dace.dmgr.combat.entity.temporary.dummy;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.task.DelayTask;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Color;
import org.bukkit.Sound;

/**
 * 총알을 발사하여 공격하는 더미의 행동 양식 클래스.
 */
@NoArgsConstructor
public final class ShootingBehavior implements DummyBehavior {
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
    private static final int DAMAGE = 100;
    /** 투사체 속력 (단위: 블록/s) */
    private static final int VELOCITY = 30;

    @Override
    public void onInit(@NonNull Dummy dummy) {
        if (!dummy.getStatusEffectModule().hasRestriction(CombatRestriction.USE_WEAPON)) {
            new DummyProjectile(dummy).shot();

            SHOOT_SOUND.play(dummy.getLocation());
        }

        dummy.addTask(new DelayTask(() -> onInit(dummy), RandomUtils.nextInt(20, 30)));
    }

    private static final class DummyProjectile extends Projectile<Damageable> {
        private DummyProjectile(@NonNull Dummy shooter) {
            super(shooter, VELOCITY, CombatUtil.EntityCondition.enemy(shooter));
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
            return createCritHitEntityHandler((location, target, isCrit) -> {
                target.getDamageModule().damage(this, DAMAGE, DamageType.NORMAL, location, isCrit, false);
                return false;
            });
        }
    }
}
