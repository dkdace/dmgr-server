package com.dace.dmgr.combat.combatant.no7;

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
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public final class No7UltInfo extends UltimateSkillInfo<No7Ult> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 8000;
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(2);
    /** 피해량 */
    public static final int DAMAGE = 500;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 8;
    /** 기절 시간 */
    public static final Timespan STUN_DURATION = Timespan.ofSeconds(2.5);

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 10;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;

    @Getter
    private static final No7UltInfo instance = new No7UltInfo();

    private No7UltInfo() {
        super(No7Ult.class, "일렉트릭 쇼크",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("강력한 아크 방전을 일으켜 <:DAMAGE:광역 피해>를 입히고 긴 시간동안 <:STUN:기절>시킵니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.STUN, Format.TIME, STUN_DURATION.toSeconds())
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 번개 효과 */
        public static final PlayableEffect LIGHTNING = PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, No7A3Info.Effects.COLOR).build(),
                ParticleEffect.Normal.builder(Particle.CRIT).build());
        /** 사용 시 틱 효과 - 1 */
        public static final PlayableEffect.Function<Long> USE_TICK_1 = i ->
                SoundEffect.builder("random.charge").volume(3).pitch(0.8 + i * 0.021).build();
        /** 사용 시 틱 효과 - 2 */
        public static final ParticleEffect USE_TICK_2 =
                ParticleEffect.Normal.builder(Particle.FIREWORKS_SPARK).count(3).horizontalSpread(0.1).verticalSpread(0.1).speed(0.1).build();
        /** 사용 준비 - 1 */
        public static final PlayableEffect USE_READY_1 = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(5).pitch(1.2).build(),
                SoundEffect.builder(Sound.ENTITY_LIGHTNING_THUNDER).volume(5).pitch(1.2).build(),
                SoundEffect.builder(Sound.ENTITY_LIGHTNING_THUNDER).volume(5).pitch(1.4).build(),
                SoundEffect.builder("random.explosion").volume(5).pitch(2).build(),
                SoundEffect.builder("random.explosion_reverb").volume(7).pitch(0.9).build());
        /** 사용 준비 - 2 */
        public static final PlayableEffect USE_READY_2 = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.FIREWORKS_SPARK).count(500).horizontalSpread(0.2).verticalSpread(0.2).speed(0.5).build(),
                ParticleEffect.Normal.builder(Particle.CRIT).count(600).horizontalSpread(0.3).verticalSpread(0.3).speed(1.2).build());

        private static void playLightning(@NonNull Location location, boolean isReady, int count, @NonNull Vector velocity) {
            Vector vec = velocity.clone().normalize();

            for (Location loc : LocationUtil.getIterable(location, vec.clone().multiply(0.3), isReady ? 3 : 1)) {
                location = loc;
                LIGHTNING.play(loc);
            }

            if (count > 0)
                for (int i = 0; i < 2; i++)
                    playLightning(location, isReady, count - 1, VectorUtil.getSpreadedVector(vec, 100));
        }

        /**
         * 사용 시 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playUseTick(long i, @NonNull Location location) {
            USE_TICK_1.apply(i).play(location);

            location = location.clone().add(0, 1, 0);

            USE_TICK_2.play(location);
            playLightning(location, false, 1, VectorUtil.getRandomVector());
        }

        /**
         * 사용 준비 효과를 재생한다.
         *
         * @param location 위치
         */
        public static void playUseReady(@NonNull Location location) {
            USE_READY_1.play(location);
            USE_READY_2.play(location.clone().add(0, 1, 0));
        }

        /**
         * 틱 효과를 재생한다.
         *
         * @param location 위치
         */
        public static void playTick(@NonNull Location location) {
            for (int i = 0; i < 6; i++)
                playLightning(location, true, 2, VectorUtil.getRandomVector());
        }
    }
}
