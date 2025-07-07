package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public final class ChedA3Info extends ActiveSkillInfo<ChedA3> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(24);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.6);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 60;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 30;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 7;
    /** 탐지 시간 */
    public static final Timespan DETECT_DURATION = Timespan.ofSeconds(6);

    /** 탐지 점수 */
    public static final CombatScore DETECT_SCORE = new CombatScore("적 탐지", 5);
    /** 처치 점수 */
    public static final CombatScore KILL_SCORE = new CombatScore("탐지 보너스", 10);
    /** 처치 점수 제한시간 */
    public static final Timespan KILL_SCORE_TIME_LIMIT = Timespan.ofSeconds(7);

    @Getter
    private static final ChedA3Info instance = new ChedA3Info();

    private ChedA3Info() {
        super(ChedA3.class, "고스트 피닉스",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("벽을 관통하는 유령 불사조를 날려보내 범위에 닿은 적을 탐지하여 아군에게 표시합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, SIZE)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DETECT_DURATION.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(2).pitch(1.6).build(),
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.7).build(),
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.7).build());
        /** 사용 시 틱 효과 */
        public static final PlayableEffect.Function<Vector> USE_TICK = velocity -> PlayableEffect.list(
                ParticleEffect.Directional.create(Particle.CRIT_MAGIC, velocity.clone().multiply(-0.25)),
                ParticleEffect.Directional.create(Particle.SMOKE_NORMAL, velocity.clone().multiply(0.12)));
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_ENDERDRAGON_FLAP).volume(1.5).pitch(1.4).build(),
                SoundEffect.builder(Sound.ENTITY_VEX_CHARGE).volume(1.5).pitch(1.3).build(),
                SoundEffect.builder(Sound.ENTITY_VEX_AMBIENT).volume(1.5).pitch(1.7).build(),
                SoundEffect.builder(Sound.ENTITY_VEX_AMBIENT).volume(1.5).pitch(1.5).build());
        /** 총알 궤적 - 1 */
        public static final PlayableEffect BULLET_TRAIL_1 = PlayableEffect.list(
                SoundEffect.builder("new.entity.phantom.flap").volume(1).pitch(1.3).build(),

                ParticleEffect.Normal.builder(Particle.CRIT_MAGIC).count(20).horizontalSpread(0.28).verticalSpread(0.28).build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, Color.fromRGB(64, 160, 184)).count(15)
                        .horizontalSpread(2.5).verticalSpread(1.5).build());
        /** 총알 궤적 - 2 */
        public static final PlayableEffect.BiFunction<Double, Double> BULLET_TRAIL_2 = (horizontalSpread, verticalSpread) ->
                ParticleEffect.Normal.builder(Particle.CRIT_MAGIC).count(8).horizontalSpread(horizontalSpread).verticalSpread(verticalSpread).build();

        /**
         * 사용 시 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playUseTick(long i, @NonNull Location location) {
            location = LocationUtil.getLocationFromOffset(location, 0, 0, 1.5);

            Vector vector = VectorUtil.getYawAxis(location);
            Vector axis = VectorUtil.getRollAxis(location);

            for (int j = 0; j < 2; j++) {
                long index = i * 2 + j;
                long index1 = Math.min(index, 24);
                double angle = index1 * 8.0;
                double distance = index1 * 0.04;
                double forward = 0;

                if (index1 == 24) {
                    long subIndex = index - index1;
                    angle += subIndex * 4;
                    distance -= subIndex * 0.03;
                    forward = subIndex * 0.2;
                }

                for (int k = 0; k < 10; k++) {
                    angle += 360 / 5.0;
                    Vector vec1 = VectorUtil.getRotatedVector(vector, axis, angle);
                    Vector vec2 = VectorUtil.getRotatedVector(vector, axis, angle + 10.0);

                    Vector vec = LocationUtil.getDirection(location.clone().add(vec1), location.clone().add(vec2));
                    Location loc = location.clone()
                            .add(vec1.clone().multiply(distance + (k < 5 ? 0 : 1.4)))
                            .add(location.getDirection().multiply(forward));

                    USE_TICK.apply(vec).play(loc);
                }
            }
        }

        /**
         * 총알 궤적을 재생한다.
         *
         * @param location 위치
         */
        public static void playBulletTrail(@NonNull Location location) {
            location.setPitch(0);

            BULLET_TRAIL_1.play(location);

            BULLET_TRAIL_2.apply(0.2, 0.12).play(LocationUtil.getLocationFromOffset(location, 0, -0.5, -0.6));
            BULLET_TRAIL_2.apply(0.16, 0.08).play(LocationUtil.getLocationFromOffset(location, 0, -0.7, -1.2));
            BULLET_TRAIL_2.apply(0.12, 0.04).play(LocationUtil.getLocationFromOffset(location, 0, -0.9, -1.8));

            BULLET_TRAIL_2.apply(0.1, 0.16).play(LocationUtil.getLocationFromOffset(location, 0, 0.4, 0.8));
            BULLET_TRAIL_2.apply(0.1, 0.16).play(LocationUtil.getLocationFromOffset(location, 0, 0.6, 1));
            BULLET_TRAIL_2.apply(0.18, 0.16).play(LocationUtil.getLocationFromOffset(location, 0, 0.8, 1.4));
            BULLET_TRAIL_2.apply(0.24, 0.16).play(LocationUtil.getLocationFromOffset(location, 0, 0.8, 1.6));

            for (int i = 0; i < 6; i++) {
                BULLET_TRAIL_2.apply(0.1, 0.1 + i * 0.04)
                        .play(LocationUtil.getLocationFromOffset(location, 0.7 + i * 0.4, 0.3 + i * (i < 3 ? 0.2 : 0.25), 0));
                BULLET_TRAIL_2.apply(0.1, 0.1 + i * 0.04)
                        .play(LocationUtil.getLocationFromOffset(location, -0.7 - i * 0.4, 0.3 + i * (i < 3 ? 0.2 : 0.25), 0));
            }
        }
    }
}
