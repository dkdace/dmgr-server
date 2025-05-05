package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.DynamicTraitInfo;
import com.dace.dmgr.combat.combatant.quaker.QuakerWeaponInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class SiliaT2Info extends DynamicTraitInfo<SiliaT2> {
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(0.3);
    /** 피해량 */
    public static final int DAMAGE = 350;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 3.7;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 0.5;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 1;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 5;

    @Getter
    private static final SiliaT2Info instance = new SiliaT2Info();

    private SiliaT2Info() {
        super(SiliaT2.class, "일격",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("특수 공격으로, 칼을 휘둘러 근거리에 <:DAMAGE:광역 피해>를 입히고 <:KNOCKBACK:밀쳐냅니다>.")
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_SWEEP).volume(1.5).pitch(1, 1.2).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_IRONGOLEM_ATTACK).volume(1.5).pitch(0.8, 1).build(),
                SoundEffect.SoundInfo.builder("random.swordhit").volume(1.5).pitch(0.7, 0.9).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class Particles {
        /** 총알 궤적 (중심) */
        public static final ParticleEffect BULLET_TRAIL_CORE = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, QuakerWeaponInfo.Particles.COLOR)
                        .count(8).horizontalSpread(0.15).verticalSpread(0.15).build());
        /** 총알 궤적 (장식) */
        public static final ParticleEffect BULLET_TRAIL_DECO = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(15).horizontalSpread(0.08).verticalSpread(0.08).speed(0.08).build());
        /** 타격 */
        public static final ParticleEffect HIT = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_NORMAL).count(3).horizontalSpread(0.05).verticalSpread(0.05).speed(0.05)
                        .build());
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(40).speed(0.4).build());
    }
}
