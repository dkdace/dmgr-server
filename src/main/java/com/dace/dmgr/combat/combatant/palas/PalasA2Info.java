package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class PalasA2Info extends ActiveSkillInfo<PalasA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(15);
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 40;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(2.5);

    /** 사용 점수 */
    public static final int USE_SCORE = 5;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;

    @Getter
    private static final PalasA2Info instance = new PalasA2Info();

    private PalasA2Info() {
        super(PalasA2.class, "생체 나노봇: 알파-X",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("바라보는 아군에게 나노봇을 투여하여 일정 시간동안 모든 <:NEGATIVE_EFFECT:해로운 효과>에 면역시킵니다. " +
                                "<d::생체 나노봇： 아드레날린> 효과를 덮어씁니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_FIREWORK_SHOOT).volume(2).pitch(1.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_SWIM).volume(2).pitch(1.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_SWIM).volume(2).pitch(2).build());
        /** 엔티티 타격 */
        public static final SoundEffect HIT_ENTITY = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.entity.puffer_fish.blow_out").volume(2).pitch(1.8).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 엔티티 타격 (중심) */
        public static final ParticleEffect HIT_ENTITY_CORE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_NORMAL).count(40).horizontalSpread(0.5).verticalSpread(0.5).speed(0.2)
                        .build());
        /** 엔티티 타격 (장식) */
        public static final ParticleEffect HIT_ENTITY_DECO = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(255, 230, 90)).count(2).horizontalSpread(0.1).verticalSpread(0.1).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.SPELL_INSTANT).build());
        /** 틱 입자 효과 */
        public static final ParticleEffect TICK = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(255, 230, 90)).count(4).horizontalSpread(1).verticalSpread(1.5).build());
    }
}
