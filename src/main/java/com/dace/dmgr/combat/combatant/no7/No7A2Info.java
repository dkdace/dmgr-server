package com.dace.dmgr.combat.combatant.no7;

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

public final class No7A2Info extends ActiveSkillInfo<No7A2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(8);
    /** 크기 (단위: 블록) */
    public static final double SIZE = 7;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(2.5);

    /** 방어 점수 */
    public static final int BLOCK_SCORE = 5;

    @Getter
    private static final No7A2Info instance = new No7A2Info();

    private No7A2Info() {
        super(No7A2.class, "능동방어 자기장",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("일정 시간동안 자기장을 둘러 주변으로 날아오는 탄환 및 투사체를 소멸시킵니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, SIZE / 2)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2, ActionKey.LEFT_CLICK)
                        .build(),
                        new AbilityInfoLore.NamedSection("지속시간 종료/재사용 시", AbilityInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("해제", ActionKey.SLOT_2, ActionKey.LEFT_CLICK)
                                .build())));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 활성화 */
        public static final PlayableEffect ON = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_PISTON_EXTEND).volume(1.5).pitch(0.5).build(),
                SoundEffect.builder("new.block.beacon.activate").volume(1.5).pitch(1.4).build());
        /** 틱 효과 - 1 */
        public static final SoundEffect TICK_1 =
                SoundEffect.builder(Sound.ENTITY_GUARDIAN_ATTACK).volume(1).pitch(1.5).build();
        /** 틱 효과 - 2 */
        public static final PlayableEffect.Function<Vector> TICK_2 = velocity -> PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, Color.fromRGB(255, 255, 176)).build(),
                ParticleEffect.Directional.create(Particle.CRIT, velocity.clone().multiply(-0.5)));
        /** 비활성화 */
        public static final PlayableEffect OFF = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_PISTON_CONTRACT).volume(1.5).pitch(0.5).build());
        /** 피격 */
        public static final PlayableEffect DAMAGE = PlayableEffect.list(
                SoundEffect.builder("new.entity.puffer_fish.blow_out").volume(0.5).pitch(1.2).pitchVariance(0.05).build(),

                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).count(5).speed(0.05).build());

        /**
         * 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 사용 위치
         * @param prev     이전 위치
         */
        public static void playTick(long i, @NonNull Location location, @NonNull Location prev) {
            TICK_1.play(location);

            location = location.clone().add(0, 1, 0);
            location.setYaw(prev.getYaw());
            location.setPitch(0);

            Vector vector = VectorUtil.getPitchAxis(location);
            Vector axis = VectorUtil.getYawAxis(location);

            for (int j = 0; j < 4; j++) {
                long index = i * 3 + j;
                double yaw = index * 7.0;
                double pitch = -90;

                for (int k = 0; k < 4; k++) {
                    yaw += 360 / 4.0;
                    pitch += 180 / 4.0;
                    Vector vec = VectorUtil.getRotatedVector(axis, VectorUtil.getRotatedVector(vector, axis, pitch), yaw);
                    Location loc2 = location.clone().add(vec.clone().multiply(2.5));

                    TICK_2.apply(vec).play(loc2);
                }
            }
        }
    }
}
