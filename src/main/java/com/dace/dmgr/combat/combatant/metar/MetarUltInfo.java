package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.UltimateSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.VectorUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

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
                new AbilityInfoLore(AbilityInfoLore.Section
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
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(30, 255, 180);

        /** 사용 시 틱 효과 */
        public static final PlayableEffect.Function<Long> USE_TICK = i -> PlayableEffect.list(
                SoundEffect.builder("random.charge").volume(3).pitch(0.5 + i * 0.0334).build(),
                SoundEffect.builder(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS).volume(3).pitch(0.5 + i * 0.023).build(),

                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).horizontalSpread(0.3).verticalSpread(0.3).count(5)
                        .build(),
                ParticleEffect.Normal.builder(Particle.CRIT_MAGIC).count(5).speed(0.3).build());
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder("random.energy").volume(5).pitch(0.5).build(),
                SoundEffect.builder("new.block.conduit.deactivate").volume(5).pitch(0.8).build(),
                SoundEffect.builder("random.explosion").volume(5).pitch(0.5).build(),
                SoundEffect.builder("random.explosion_reverb").volume(7).pitch(0.5).build(),

                ParticleEffect.Normal.builder(Particle.EXPLOSION_LARGE).count(10).horizontalSpread(0.6).verticalSpread(0.6).build());
        /** 틱 효과 - 1 */
        public static final PlayableEffect TICK_1 = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.PORTAL).count(15).speed(2.5).build(),
                ParticleEffect.Normal.builder(Particle.CRIT_MAGIC).count(8).horizontalSpread(0.2).verticalSpread(0.2).speed(0.2).build(),
                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).count(15).horizontalSpread(0.3).verticalSpread(0.3).build());
        /** 틱 효과 - 2 */
        public static final SoundEffect TICK_2 =
                SoundEffect.builder("random.charge").volume(3).pitch(0.6).build();
        /** 총알 궤적 - 1 */
        public static final PlayableEffect BULLET_TRAIL_1 = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.VILLAGER_HAPPY).count(5).horizontalSpread(0.3).verticalSpread(0.3).build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).horizontalSpread(0.7).verticalSpread(0.7).count(12)
                        .build());
        /** 총알 궤적 - 2 */
        public static final ParticleEffect BULLET_TRAIL_2 =
                ParticleEffect.Normal.builder(Particle.VILLAGER_HAPPY).count(2).horizontalSpread(0.08).verticalSpread(0.08).build();

        /**
         * 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playTick(long i, @NonNull Location location) {
            TICK_1.play(location);
            if (i % 4 == 0)
                TICK_2.play(location);
        }

        /**
         * 총알 궤적을 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playBulletTrail(long i, @NonNull Location location) {
            BULLET_TRAIL_1.play(location);

            if (i % 3 == 0) {
                Vector vector = VectorUtil.getYawAxis(location).multiply(2);
                Vector axis = VectorUtil.getRollAxis(location);

                for (int j = 0; j < 16; j++) {
                    double angle = 360 / 16.0 * j;
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);
                    Location loc = location.clone().add(vec);

                    BULLET_TRAIL_2.play(loc);
                }
            }
        }
    }
}
