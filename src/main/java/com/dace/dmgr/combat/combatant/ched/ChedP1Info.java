package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Sound;

public final class ChedP1Info extends PassiveSkillInfo<ChedP1> {
    /** 벽타기 이동 강도 */
    public static final double PUSH = 0.45;
    /** 벽타기 최대 횟수 */
    public static final int USE_COUNT = 10;
    /** 매달리기 최대 시간 */
    public static final Timespan HANG_DURATION = Timespan.ofSeconds(6);

    @Getter
    private static final ChedP1Info instance = new ChedP1Info();

    private ChedP1Info() {
        super(ChedP1.class, "궁사의 날렵함",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("벽을 클릭하여 벽을 오를 수 있습니다. 오르는 도중 <3::매달리기>를 할 수 있습니다.")
                        .addActionKeyInfo("사용", ActionKey.LEFT_CLICK)
                        .build(),
                        new ActionInfoLore.NamedSection("매달리기", ActionInfoLore.Section
                                .builder("벽에 매달려 위치를 고정합니다.")
                                .addValueInfo(TextIcon.DURATION, Format.TIME, HANG_DURATION.toSeconds())
                                .addActionKeyInfo("사용", ActionKey.SNEAK)
                                .build()),
                        new ActionInfoLore.NamedSection("매달리기: 지속시간 종료/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addActionKeyInfo("해제", ActionKey.SNEAK)
                                .build())));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_STONE_STEP).volume(1).pitch(0.525).pitchVariance(0.05).build());
        /** 사용 (매달리기) */
        public static final SoundEffect USE_HANG = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.entity.phantom.flap").volume(1).pitch(1.7).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(0.6).pitch(0.85).build());
        /** 해제 (매달리기) */
        public static final SoundEffect DISABLE_HANG = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.entity.phantom.flap").volume(1).pitch(1.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_LLAMA_SWAG).volume(0.6).pitch(1.4).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class Particles {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(186, 55, 30);

        /** 사용 (매달리기) */
        public static final ParticleEffect USE_HANG = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.SPELL_MOB, COLOR).count(40)
                        .horizontalSpread(0.65).build());
        /** 틱 입자 효과 (매달리기) */
        public static final ParticleEffect TICK_HANG = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, COLOR).horizontalSpread(0.24)
                        .build());
    }
}
