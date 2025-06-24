package com.dace.dmgr.combat.combatant.vellion;

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
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public final class VellionA3Info extends ActiveSkillInfo<VellionA3> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(17);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.6);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 30;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(6);

    /** 초당 효과 점수 */
    public static final int EFFECT_SCORE_PER_SECOND = 2;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 15;

    @Getter
    private static final VellionA3Info instance = new VellionA3Info();

    private VellionA3Info() {
        super(VellionA3.class, "칠흑의 균열",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("바라보는 곳에 균열을 일으켜 범위의 적을 <:SILENCE:침묵>시키고 <:HEAL_BAN:회복을 차단>합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(156, 60, 130);

        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.7).build(),
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.7).build(),
                SoundEffect.builder(Sound.ENTITY_GUARDIAN_HURT).volume(2).pitch(2).build());
        /** 사용 시 틱 효과 - 1 */
        public static final PlayableEffect USE_TICK_1 = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.SMOKE_LARGE).count(30).horizontalSpread(0.5).verticalSpread(0.3).speed(0.15).build(),
                ParticleEffect.Normal.builder(Particle.SPELL_WITCH).count(70).horizontalSpread(1).verticalSpread(0.5).speed(0.2).build());
        /** 사용 시 틱 효과 - 2 */
        public static final PlayableEffect USE_TICK_2 = PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).build());
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS).volume(2).pitch(0.9).build(),
                SoundEffect.builder("new.block.respawn_anchor.set_spawn").volume(2).pitch(0.6).build(),
                SoundEffect.builder("new.block.respawn_anchor.set_spawn").volume(2).pitch(0.7).build());
        /** 틱 효과 - 1 */
        public static final ParticleEffect TICK_1 =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, COLOR).count(3).horizontalSpread(0.4).build();
        /** 틱 효과 - 2 */
        public static final PlayableEffect.Function<Vector> TICK_2 = velocity -> PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).build(),
                ParticleEffect.Directional.create(Particle.SMOKE_NORMAL, velocity.clone().multiply(0.1)));
        /** 틱 효과 - 3 */
        public static final PlayableEffect.Function<Vector> TICK_3 = velocity ->
                ParticleEffect.Directional.create(Particle.SMOKE_LARGE, velocity.clone().multiply(0.3));

        /**
         * 사용 시 틱 효과를 재생한다.
         *
         * @param location 사용 위치
         * @param target   대상 위치
         */
        public static void playUseTick(@NonNull Location location, @NonNull Location target) {
            USE_TICK_1.play(target);
            for (Location loc : LocationUtil.getLine(location, target, 0.7))
                USE_TICK_2.play(loc);
        }

        /**
         * 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playTick(long i, @NonNull Location location) {
            TICK_1.play(location);

            location = location.clone();
            location.setYaw(0);
            location.setPitch(0);

            Vector vector = VectorUtil.getRollAxis(location);
            Vector axis = VectorUtil.getYawAxis(location);

            for (int j = 0; j < 2; j++) {
                long index = i * 2 + j;
                double angle = index * 3.0;
                double distance = index * 0.2 % 5;

                for (int k = 0; k < 12; k++) {
                    angle += 360 / (distance > 3 ? 4.0 : 6.0);
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 6 ? angle : -angle);
                    Location loc = location.clone().add(vec.clone().multiply(distance));

                    TICK_2.apply(vec.setY(0.4)).play(loc);
                }

                double angle2 = index * 44.0;
                double up = index * 0.1 % 2.5;

                for (int k = 0; k < 3; k++) {
                    angle2 += 360 / 3.0;
                    Vector vec1 = VectorUtil.getRotatedVector(vector, axis, angle2);
                    Vector vec2 = VectorUtil.getRotatedVector(vector, axis, angle2 + 10.0);

                    Vector vec = LocationUtil.getDirection(location.clone().add(vec1), location.clone().add(vec2)).setY(up * 0.1);
                    Location loc = location.clone().add(vec1.clone().multiply(5)).add(0, up * 0.5, 0);

                    TICK_3.apply(vec).play(loc);
                }
            }
        }
    }
}
