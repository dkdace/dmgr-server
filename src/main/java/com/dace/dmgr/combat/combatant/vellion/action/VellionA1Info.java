package com.dace.dmgr.combat.combatant.vellion.action;

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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class VellionA1Info extends ActiveSkillInfo<VellionA1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(6);
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(1.7);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.5);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 초당 독 피해량 */
    public static final int POISON_DAMAGE_PER_SECOND = 120;
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 160;
    /** 효과 지속 시간 */
    public static final Timespan EFFECT_DURATION = Timespan.ofSeconds(2.5);
    /** 속박 시간 */
    public static final Timespan SNARE_DURATION = Timespan.ofSeconds(0.1);
    /** 회수 시간 */
    public static final Timespan RETURN_DURATION = Timespan.ofSeconds(0.75);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 2.5;

    /** 효과 점수 */
    public static final int EFFECT_SCORE = 1;

    @Getter
    private static final VellionA1Info instance = new VellionA1Info();

    private VellionA1Info() {
        super(VellionA1.class, "마력 집중",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("개체를 관통하는 마력 응집체를 날려 적에게는 <:POISON:독 피해>와 짧은 <:SNARE:속박>을 입히고, 아군에게는 지속적인 <:HEAL:치유> 효과를 줍니다. " +
                                "벽이나 최대 사거리에 도달하면 되돌아오며 효과를 다시 입힙니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.POISON, Format.TIME_WITH_PER_SECOND, EFFECT_DURATION.toSeconds(), POISON_DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.SNARE, Format.TIME, SNARE_DURATION.toSeconds())
                        .addValueInfo(TextIcon.HEAL, Format.TIME_WITH_PER_SECOND, EFFECT_DURATION.toSeconds(), HEAL_PER_SECOND)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, VELOCITY * RETURN_DURATION.toSeconds())
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1, ActionKey.RIGHT_CLICK)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDEREYE_DEATH).volume(2).pitch(0.8).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDEREYE_DEATH).volume(2).pitch(0.8).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED).volume(2).pitch(1.5).build());
        /** 사용 준비 */
        public static final SoundEffect USE_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_SHULKER_SHOOT).volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_MIRROR).volume(2).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ELDER_GUARDIAN_DEATH).volume(2).pitch(1.8).build());
        /** 엔티티 타격 */
        public static final SoundEffect HIT_ENTITY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_INFECT).volume(1).pitch(0.7).pitchVariance(0.05).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 사용 시 틱 입자 효과 - 1 */
        public static final ParticleEffect USE_TICK_1 = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(0, ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        45, 109, 0, 0, 240, 192).build());
        /** 사용 시 틱 입자 효과 - 2 */
        public static final ParticleEffect USE_TICK_2 = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.SPELL_WITCH).build());
        /** 표시 */
        public static final ParticleEffect DISPLAY = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 120, 0, 220)
                        .count(20).horizontalSpread(0.5).verticalSpread(0.5).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.SPELL_MOB, 140, 120, 180)
                        .count(10).horizontalSpread(1.5).verticalSpread(1.5).build(),
                ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.FALLING_DUST, Material.WOOL, 10).count(8)
                        .horizontalSpread(0.5).verticalSpread(0.5).speed(0.05).build());
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT_MAGIC).count(30).speed(0.4).build());
    }
}
