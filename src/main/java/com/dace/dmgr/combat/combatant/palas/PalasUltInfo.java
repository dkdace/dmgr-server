package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.effect.FireworkEffect;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class PalasUltInfo extends UltimateSkillInfo<PalasUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 9500;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 40;
    /** 공격력 증가량 */
    public static final int DAMAGE_INCREMENT = 60;
    /** 이동속도 증가량 */
    public static final int SPEED_INCREMENT = 40;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(7);

    /** 사용 점수 */
    public static final CombatScore USE_SCORE = new CombatScore("아군 강화", 10);
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 30;

    @Getter
    private static final PalasUltInfo instance = new PalasUltInfo();

    private PalasUltInfo() {
        super(PalasUlt.class, "생체 나노봇: 아드레날린",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("바라보는 아군에게 나노봇을 투여하여 일정 시간동안 <:DAMAGE_INCREASE:공격력>과 <:WALK_SPEED_INCREASE:이동 속도>를 증폭시킵니다. " +
                                "<d::생체 나노봇： 알파-X> 효과를 덮어씁니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE_INCREASE, Format.PERCENT, DAMAGE_INCREMENT)
                        .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED_INCREMENT)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(255, 70, 75);

        /** 사용 - 1 */
        public static final PlayableEffect USE_1 = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_FIREWORK_SHOOT).volume(3).pitch(1.6).build(),
                SoundEffect.builder(Sound.ENTITY_PLAYER_SWIM).volume(3).pitch(1.6).build(),
                SoundEffect.builder(Sound.ENTITY_PLAYER_SWIM).volume(3).pitch(1.8).build());
        /** 사용 - 2 */
        public static final PlayableEffect USE_2 = PlayableEffect.list(
                SoundEffect.builder("new.item.trident.thunder").volume(3).pitch(1.5).build(),

                ParticleEffect.Normal.builder(Particle.EXPLOSION_NORMAL).count(40).horizontalSpread(0.5).verticalSpread(0.5).speed(0.2).build());
        /** 사용 - 3 */
        public static final PlayableEffect USE_3 = PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).count(2).horizontalSpread(0.1).verticalSpread(0.1)
                        .build(),
                ParticleEffect.Normal.builder(Particle.SPELL_INSTANT).build());
        /** 사용 폭죽 효과 */
        public static final FireworkEffect USE_FIREWORK = FireworkEffect.builder(org.bukkit.FireworkEffect.Type.BURST, COLOR)
                .fadeColor(Color.fromRGB(200, 0, 0)).build();
        /** 버프 - 틱 효과 */
        public static final PlayableEffect BUFF_TICK = PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).count(4).horizontalSpread(1).verticalSpread(1.5)
                        .build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, Color.fromRGB(255, 50, 24)).count(2)
                        .horizontalSpread(1).verticalSpread(1.5).build());

        /**
         * 사용 효과를 재생한다.
         *
         * @param location 사용 위치
         * @param start    시작 위치
         * @param end      끝 위치
         */
        public static void playUse(@NonNull Location location, @NonNull Location start, @NonNull Location end) {
            USE_1.play(location);
            USE_2.play(end);
            USE_FIREWORK.play(end);

            for (Location loc : LocationUtil.getLine(start, end, 0.4))
                USE_3.play(loc);
        }
    }
}
