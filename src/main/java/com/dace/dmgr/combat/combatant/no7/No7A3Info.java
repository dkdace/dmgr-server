package com.dace.dmgr.combat.combatant.no7;

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
import org.bukkit.*;
import org.bukkit.util.Vector;

public final class No7A3Info extends ActiveSkillInfo<No7A3> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(9);
    /** 보호막 */
    public static final int SHIELD = 300;
    /** 감지 범위 (단위: 블록) */
    public static final double DETECT_RADIUS = 8;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(1);

    /** 회복 점수 */
    public static final CombatScore SHIELD_SCORE = new CombatScore("보호막 획득", 4);

    @Getter
    private static final No7A3Info instance = new No7A3Info();

    private No7A3Info() {
        super(No7A3.class, "적응형 보호막",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("일정 시간동안 <e:HEAL:보호막>을 얻습니다. 근처에 적이 많을 수록 획득량이 증가합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.HEAL, "{0}+(적 수)×{1}", ChatColor.YELLOW, SHIELD, SHIELD)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, DETECT_RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(255, 255, 43);

        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(1.5).pitch(1.4).build(),
                SoundEffect.builder(Sound.BLOCK_PISTON_CONTRACT).volume(1.5).pitch(1.6).build());
        /** 틱 효과 - 1 */
        public static final SoundEffect TICK_1 =
                SoundEffect.builder("random.charge").volume(0.6).pitch(1.2).build();
        /** 틱 효과 - 2 */
        public static final ParticleEffect TICK_2 =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).count(5).verticalSpread(0.7).build();
        /** 틱 효과 - 3 */
        public static final PlayableEffect.Function<Vector> TICK_3 = velocity ->
                ParticleEffect.Directional.create(Particle.CRIT, velocity.clone().multiply(0.5));

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

            Vector vector = VectorUtil.getRollAxis(location).multiply(3 - i * 0.12);
            Vector axis = VectorUtil.getYawAxis(location);

            double angle = i * 4.0;
            for (int j = 0; j < 6; j++) {
                angle += 360 / 6.0;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);
                Location loc = location.clone().add(vec);

                TICK_2.play(loc);

                for (int k = 0; k < 2; k++) {
                    Location loc2 = loc.clone().add(0, -0.5 + k, 0);
                    Vector vec2 = LocationUtil.getDirection(loc2, location);

                    TICK_3.apply(vec2).play(loc2);
                }
            }
        }
    }
}
