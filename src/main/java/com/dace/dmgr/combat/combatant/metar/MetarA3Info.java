package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.ActiveSkillInfo;
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

public final class MetarA3Info extends ActiveSkillInfo<MetarA3> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(11);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.3);
    /** 폭파 시간 */
    public static final Timespan EXPLODE_DURATION = Timespan.ofSeconds(6);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 15;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.5;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5.5;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(0.7);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.4;

    /** 효과 점수 */
    public static final int EFFECT_SCORE = 8;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;
    /** 처치 지원 점수 제한시간 */
    public static final Timespan ASSIST_SCORE_TIME_LIMIT = Timespan.ofSeconds(1.4);

    @Getter
    private static final MetarA3Info instance = new MetarA3Info();

    private MetarA3Info() {
        super(MetarA3.class, "중력장 발생기",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("중력장 발생기를 발사합니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, EXPLODE_DURATION.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build(),
                        new AbilityInfoLore.NamedSection("지속시간 종료/재사용 시", AbilityInfoLore.Section
                                .builder("발생기에서 중력장을 방출하여 주변의 적을 <:KNOCKBACK:끌어당깁니다>.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                                .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                                .addActionKeyInfo("격발", ActionKey.SLOT_3)
                                .build())));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final SoundEffect USE =
                SoundEffect.builder(Sound.ENTITY_CAT_PURREOW).volume(0.5).pitch(1.6).build();
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_SHULKER_BULLET_HIT).volume(3).pitch(1.4).build(),
                SoundEffect.builder(Sound.ENTITY_GHAST_SHOOT).volume(3).pitch(1.2).build(),
                SoundEffect.builder("random.energy").volume(3).pitch(1.6).build());
        /** 총알 궤적 - 1 */
        public static final PlayableEffect BULLET_TRAIL_1 = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.PORTAL).count(15).horizontalSpread(0.1).verticalSpread(0.1).speed(0.3).build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, Color.fromRGB(0, 0, 0))
                        .horizontalSpread(0.3).verticalSpread(0.3).count(12).build(),
                ParticleEffect.Normal.builder(Particle.DRAGON_BREATH).count(5).horizontalSpread(0.05).verticalSpread(0.05).build());
        /** 총알 궤적 - 2 */
        public static final SoundEffect BULLET_TRAIL_2 =
                SoundEffect.builder("new.block.beacon.ambient").volume(0.6).pitch(1.8).build();
        /** 격발 */
        public static final SoundEffect DETONATE =
                SoundEffect.builder(Sound.BLOCK_STONE_BUTTON_CLICK_ON).volume(0.5).pitch(0.8).build();
        /** 격발 시 틱 효과 - 1 */
        public static final PlayableEffect.BiFunction<Long, Vector> DETONATE_TICK_1 = (i, velocity) -> PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE,
                                Color.fromRGB((int) (228 - i * 17.5), (int) (55 - i * 4.2), (int) (205 - i * 15.7))).count(2).horizontalSpread(0.08)
                        .verticalSpread(0.08).build(),
                ParticleEffect.Directional.create(Particle.SMOKE_NORMAL, velocity.clone().multiply(0.4)));
        /** 격발 시 틱 효과 - 2 */
        public static final PlayableEffect.Function<Long> DETONATE_TICK_2 = i -> PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_ENDERMEN_TELEPORT).volume(2).pitch(1.2 + i * 0.04).build(),

                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).count(30).horizontalSpread(0.25).verticalSpread(0.25).build());

        /**
         * 총알 궤적을 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playBulletTrail(long i, @NonNull Location location) {
            BULLET_TRAIL_1.play(location);
            if (i % 4 == 0)
                BULLET_TRAIL_2.play(location);
        }

        /**
         * 격발 시 틱 효과를 재생한다.
         *
         * @param i         인덱스
         * @param location  위치
         * @param locations 효과 재생 위치 목록
         */
        public static void playDetonateTick(long i, @NonNull Location location, @NonNull Location @NonNull [] locations) {
            if (i == 0)
                for (int j = 0; j < locations.length; j++) {
                    Vector vec = VectorUtil.getRandomVector().normalize().multiply(RADIUS);
                    locations[j] = location.clone().add(vec).setDirection(vec.normalize());
                }

            for (Location loc : locations) {
                Vector vec = loc.getDirection().multiply(-0.35);
                DETONATE_TICK_1.apply(i, vec).play(loc.add(vec));
            }

            DETONATE_TICK_2.apply(i).play(location);
        }
    }
}
