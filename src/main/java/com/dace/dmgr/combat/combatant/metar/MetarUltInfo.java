package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class MetarUltInfo extends UltimateSkillInfo<MetarUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 7000;
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(2);
    /** 초당 피해량 */
    public static final int DAMAGE_PER_SECOND = 800;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 4;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(7);

    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;

    @Getter
    private static final MetarUltInfo instance = new MetarUltInfo();

    private MetarUltInfo() {
        super(MetarUlt.class, "반전자 분열포",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("강력한 광선을 방출하여 지속적인 <:DAMAGE:광역 피해>를 입힙니다. " +
                                "광선은 일정 시간동안 유지됩니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, Format.PER_SECOND, DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, SIZE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 시 틱 효과음 */
        public static final SoundEffect USE_TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.charge").volume(3).pitch(0.5, 1.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS).volume(3).pitch(0.5, 1.4).build());
        /** 사용 준비 */
        public static final SoundEffect USE_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.energy").volume(5).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder("new.block.conduit.deactivate").volume(5).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder("random.explosion").volume(5).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder("random.explosion_reverb").volume(7).pitch(0.5).build());
        /** 틱 효과음 */
        public static final SoundEffect TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.charge").volume(3).pitch(0.6).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(30, 255, 180);

        /** 사용 시 틱 입자 효과 */
        public static final ParticleEffect USE_TICK = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, COLOR).horizontalSpread(0.3)
                        .verticalSpread(0.3).count(5).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT_MAGIC).count(5).speed(0.3).build());
        /** 사용 준비 */
        public static final ParticleEffect USE_READY = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_LARGE).count(10).horizontalSpread(0.6).verticalSpread(0.6).build());
        /** 틱 입자 효과 */
        public static final ParticleEffect TICK = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.PORTAL).count(15).speed(2.5).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT_MAGIC).count(8).horizontalSpread(0.2).verticalSpread(0.2).speed(0.2).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_NORMAL).count(15).horizontalSpread(0.3).verticalSpread(0.3).build());
        /** 총알 궤적 (중심) */
        public static final ParticleEffect BULLET_TRAIL_CORE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.VILLAGER_HAPPY).count(5).horizontalSpread(0.3).verticalSpread(0.3).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, COLOR).horizontalSpread(0.7)
                        .verticalSpread(0.7).count(12).build());
        /** 총알 궤적 (장식) */
        public static final ParticleEffect BULLET_TRAIL_DECO = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.VILLAGER_HAPPY).count(2).horizontalSpread(0.08).verticalSpread(0.08).build());
    }
}
