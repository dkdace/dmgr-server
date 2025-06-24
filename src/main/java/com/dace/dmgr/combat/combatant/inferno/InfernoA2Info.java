package com.dace.dmgr.combat.combatant.inferno;

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
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public final class InfernoA2Info extends ActiveSkillInfo<InfernoA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(12);
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 60;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 회복량 감소 */
    public static final int HEAL_DECREMENT = 50;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(2.5);

    /** 초당 효과 점수 */
    public static final int EFFECT_SCORE_PER_SECOND = 3;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 15;

    @Getter
    private static final InfernoA2Info instance = new InfernoA2Info();

    private InfernoA2Info() {
        super(InfernoA2.class, "불꽃 방출",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("일정 시간동안 주변에 불꽃을 방출하여 <:FIRE:화염 피해>와 <:HEAL_DECREASE:회복량> 감소를 입히고 <:GROUNDING:고정>시킵니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.FIRE, Format.PER_SECOND, FIRE_DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.HEAL_DECREASE, Format.PERCENT, HEAL_DECREMENT)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_PISTON_CONTRACT).volume(2).pitch(0.5).build(),
                SoundEffect.builder(Sound.BLOCK_PISTON_CONTRACT).volume(2).pitch(0.6).build());
        /** 틱 효과 - 1 */
        public static final PlayableEffect TICK_1 = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(2).pitch(0.55).pitchVariance(0.1).build(),
                SoundEffect.builder(Sound.BLOCK_FIRE_AMBIENT).volume(2).pitch(0.6).pitchVariance(0.1).build());
        /** 틱 효과 - 2 */
        public static final ParticleEffect TICK_2 =
                ParticleEffect.Normal.builder(Particle.FLAME).count(2).horizontalSpread(0.1).verticalSpread(0.1).speed(0.2).build();
        /** 틱 효과 - 3 */
        public static final PlayableEffect.Function<Vector> TICK_3 = velocity -> PlayableEffect.list(
                ParticleEffect.Directional.create(Particle.SMOKE_NORMAL, velocity.clone().multiply(0.32)),
                ParticleEffect.Directional.create(Particle.FLAME, velocity.clone().multiply(0.2)));

        /**
         * 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playTick(long i, @NonNull Location location) {
            TICK_1.play(location);

            location = location.clone().add(0, 1, 0);
            location.setYaw(0);
            location.setPitch(0);

            TICK_2.play(location);

            Vector vector = VectorUtil.getRollAxis(location);
            Vector axis = VectorUtil.getYawAxis(location);

            for (int j = 0; j < 3; j++) {
                long index = i * 3 + j;
                double yaw = index * 5.0;
                double pitch = index * 7.0;

                for (int k = 0; k < 3; k++) {
                    yaw += 360 / 3.0;
                    pitch -= 360 / 3.0;
                    Vector vec = VectorUtil.getRotatedVector(axis, VectorUtil.getRotatedVector(vector, axis, yaw), pitch);
                    Location loc = location.clone().add(vec.clone().multiply(1.8));

                    TICK_3.apply(vec).play(loc);
                }
            }
        }
    }
}
