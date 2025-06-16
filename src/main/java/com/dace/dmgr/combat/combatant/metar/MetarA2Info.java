package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class MetarA2Info extends ActiveSkillInfo<MetarA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(0.5);
    /** 스택 충전 쿨타임 */
    public static final Timespan STACK_COOLDOWN = Timespan.ofSeconds(8);
    /** 최대 스택 충전량 */
    public static final int MAX_STACK = 2;
    /** 체력 */
    public static final int HEALTH = 1000;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(10);

    /** 방어 점수 */
    public static final int BLOCK_SCORE = 20;
    /** 파괴 점수 */
    public static final int DEATH_SCORE = 8;

    @Getter
    private static final MetarA2Info instance = new MetarA2Info();

    private MetarA2Info() {
        super(MetarA2.class, "에너지 방벽",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("커다란 <3::에너지 방벽>을 설치하여 공격을 방어합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME_WITH_MAX_STACK, STACK_COOLDOWN.toSeconds(), MAX_STACK)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build(),
                        new ActionInfoLore.NamedSection("에너지 방벽", ActionInfoLore.Section
                                .builder("공격을 막는 고정형 방벽입니다.")
                                .addValueInfo(TextIcon.HEALTH, HEALTH)
                                .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                                .build())));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_ENDERDRAGON_FLAP).volume(1).pitch(1.3).build(),
                SoundEffect.builder("random.charge").volume(1).pitch(0.7).build());
        /** 피격 */
        public static final PlayableEffect.BiFunction<Location, Double> DAMAGE = (location, damage) -> PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_LAVA_POP).volume(0.4 + damage * 0.001).pitch(1.5).pitchVariance(0.1).build(),
                SoundEffect.builder(Sound.ENTITY_IRONGOLEM_ATTACK).volume(0.4 + damage * 0.001).pitch(1.4).pitchVariance(0.1).build(),

                location == null ? SoundEffect.NONE : ParticleEffect.Normal.builder(Particle.CRIT_MAGIC).count((int) (damage * 0.04)).speed(0.2).build());
        /** 파괴 - 1 */
        public static final PlayableEffect DEATH_1 = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_ENDERMEN_TELEPORT).volume(2).pitch(2).build(),
                SoundEffect.builder(Sound.ENTITY_ENDERMEN_TELEPORT).volume(2).pitch(2).build(),
                SoundEffect.builder(Sound.ENTITY_ENDERDRAGON_HURT).volume(2).pitch(2).build());
        /** 파괴 - 2 */
        public static final ParticleEffect DEATH_2 =
                ParticleEffect.Normal.builder(Particle.CRIT_MAGIC).count(60).horizontalSpread(0.3).verticalSpread(0.3).speed(0.4).build();

        /**
         * 파괴 효과를 재생한다.
         *
         * @param location 위치
         */
        public static void playDeath(@NonNull Location location) {
            DEATH_1.play(location);

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 2; j++) {
                    Location loc = LocationUtil.getLocationFromOffset(location, -2.2 + i * 2.2, -0.9 + j * 1.8, 0);
                    DEATH_2.play(loc);
                }
            }
        }
    }
}
