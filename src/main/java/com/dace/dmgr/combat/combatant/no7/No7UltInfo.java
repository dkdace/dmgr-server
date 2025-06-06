package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Particle;
import org.bukkit.Sound;

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
                new ActionInfoLore(ActionInfoLore.Section
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
        /** 사용 시 틱 효과음 */
        public static final PlayableEffect.Function<Long> USE_TICK_SOUND = i ->
                SoundEffect.builder("random.charge").volume(3).pitch(0.8 + i * 0.021).build();
        /** 사용 시 틱 입자 효과 */
        public static final ParticleEffect USE_TICK_PARTICLE =
                ParticleEffect.Normal.builder(Particle.FIREWORKS_SPARK).count(3).horizontalSpread(0.1).verticalSpread(0.1).speed(0.1).build();
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(5).pitch(1.2).build(),
                SoundEffect.builder(Sound.ENTITY_LIGHTNING_THUNDER).volume(5).pitch(1.2).build(),
                SoundEffect.builder(Sound.ENTITY_LIGHTNING_THUNDER).volume(5).pitch(1.4).build(),
                SoundEffect.builder("random.explosion").volume(5).pitch(2).build(),
                SoundEffect.builder("random.explosion_reverb").volume(7).pitch(0.9).build(),

                ParticleEffect.Normal.builder(Particle.FIREWORKS_SPARK).count(500).horizontalSpread(0.2).verticalSpread(0.2).speed(0.5).build(),
                ParticleEffect.Normal.builder(Particle.CRIT).count(600).horizontalSpread(0.3).verticalSpread(0.3).speed(1.2).build());
        /** 틱 효과 */
        public static final PlayableEffect TICK = PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, No7A3Info.Effects.COLOR).build(),
                ParticleEffect.Normal.builder(Particle.CRIT).build());
    }
}
