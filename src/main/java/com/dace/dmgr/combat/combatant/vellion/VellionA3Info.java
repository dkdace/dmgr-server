package com.dace.dmgr.combat.combatant.vellion;

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
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("바라보는 곳에 균열을 일으켜 범위의 적을 <:SILENCE:침묵>시키고 <:HEAL_BAN:회복을 차단>합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GUARDIAN_HURT).volume(2).pitch(2).build());
        /** 사용 준비 */
        public static final SoundEffect USE_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS).volume(2).pitch(0.9).build(),
                SoundEffect.SoundInfo.builder("new.block.respawn_anchor.set_spawn").volume(2).pitch(0.6).build(),
                SoundEffect.SoundInfo.builder("new.block.respawn_anchor.set_spawn").volume(2).pitch(0.7).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(156, 60, 130);

        /** 사용 시 틱 입자 효과 (중심) */
        public static final ParticleEffect USE_TICK_CORE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_LARGE).count(30).horizontalSpread(0.5).verticalSpread(0.3).speed(0.15).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.SPELL_WITCH).count(70).horizontalSpread(1).verticalSpread(0.5).speed(0.2).build());
        /** 사용 시 틱 입자 효과 (장식) */
        public static final ParticleEffect USE_TICK_DECO = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, COLOR).build());
        /** 틱 입자 효과 (중심) */
        public static final ParticleEffect TICK_CORE = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.SPELL_MOB, COLOR).count(3)
                        .horizontalSpread(0.4).build());
        /** 틱 입자 효과 (장식) - 1 */
        public static final ParticleEffect TICK_DECO_1 = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, COLOR).build(),
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.SMOKE_NORMAL)
                        .speedMultiplier(0.1).build());
        /** 틱 입자 효과 (장식) - 2 */
        public static final ParticleEffect TICK_DECO_2 = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.SMOKE_LARGE)
                        .speedMultiplier(0.3).build());
    }
}
