package com.dace.dmgr.combat.combatant.vellion;

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
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class VellionUltInfo extends UltimateSkillInfo<VellionUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 9000;
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(1);
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 8;
    /** 이동 속도 감소량 */
    public static final int SLOW = 50;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(2.5);
    /** 피해량 비율 */
    public static final double DAMAGE_RATIO = 0.5;
    /** 기절 시간 */
    public static final Timespan STUN_DURATION = Timespan.ofSeconds(1);

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 15;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;

    @Getter
    private static final VellionUltInfo instance = new VellionUltInfo();

    private VellionUltInfo() {
        super(VellionUlt.class, "나락의 결계",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 무적이 되어 주변 적의 <:WALK_SPEED_DECREASE:이동 속도>를 느리게 하고 <:GROUNDING:고정>시킵니다. " +
                                "일정 시간 후 결계가 폭발하여 탈출하지 못한 적은 <:DAMAGE:광역 피해>를 입고 <:STUN:기절>합니다. " +
                                "사용 중에는 움직일 수 없습니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, Format.PERCENT, SLOW)
                        .addValueInfo(TextIcon.DAMAGE, "적 최대 체력의 {0}%", (int) (100 * DAMAGE_RATIO))
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
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(90, 0, 55);

        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.8).build(),
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.8).build(),
                SoundEffect.builder(Sound.ENTITY_GUARDIAN_HURT).volume(2).pitch(1.8).build());
        /** 사용 시 틱 효과 */
        public static final PlayableEffect USE_TICK = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.SPELL_WITCH).count(3).horizontalSpread(0.05).verticalSpread(0.05).build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, Color.fromRGB(70, 0, 45)).build());
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS).volume(3).pitch(0.7).build(),
                SoundEffect.builder(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS).volume(3).pitch(0.8).build(),
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_ATTACK).volume(3).pitch(0.85).build(),
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON).volume(3).pitch(0.7).build());
        /** 틱 효과 - 1 */
        public static final ParticleEffect TICK_1 =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, COLOR).count(4).horizontalSpread(0.3).build();
        /** 틱 효과 - 2 */
        public static final ParticleEffect TICK_2 =
                ParticleEffect.Normal.builder(Particle.PORTAL).count(40).speed(1.5).build();
        /** 틱 효과 - 3 */
        public static final ParticleEffect TICK_3 =
                ParticleEffect.Normal.builder(Particle.SPELL_WITCH).count(20).verticalSpread(2).build();
        /** 틱 효과 - 4 */
        public static final PlayableEffect.Function<Long> TICK_4 = i ->
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE,
                        Color.fromRGB((int) (30 + i * 1.95), 0, (int) (18 + i * 1.64))).build();
        /** 틱 효과 - 5 */
        public static final ParticleEffect TICK_5 =
                ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.FALLING_DUST, Material.CONCRETE, 14).build();
        /** 틱 효과 - 6 */
        public static final ParticleEffect TICK_6 =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, COLOR).count(3).horizontalSpread(0.1).verticalSpread(0.1)
                        .build();
        /** 틱 효과 - 7 */
        public static final ParticleEffect TICK_7 =
                ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.FALLING_DUST, Material.MYCEL, 0).count(4).horizontalSpread(0.15)
                        .verticalSpread(0.4).build();
        /** 폭발 */
        public static final PlayableEffect EXPLODE = PlayableEffect.list(
                SoundEffect.builder("new.block.conduit.deactivate").volume(3).pitch(0.6).build(),
                SoundEffect.builder("new.block.respawn_anchor.deplete").volume(3).pitch(0.6).build(),
                SoundEffect.builder("new.block.respawn_anchor.deplete").volume(3).pitch(0.8).build(),

                ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.STAINED_GLASS, 2).count(300)
                        .horizontalSpread(0.3).verticalSpread(0.3).speed(0.4).build(),
                ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.STAINED_GLASS, 14).count(200)
                        .horizontalSpread(0.3).verticalSpread(0.3).speed(0.4).build());
        /** 엔티티 타격 - 1 */
        public static final ParticleEffect HIT_ENTITY_1 =
                ParticleEffect.Normal.builder(Particle.CRIT_MAGIC).count(50).speed(0.4).build();
        /** 엔티티 타격 - 2 */
        public static final ParticleEffect HIT_ENTITY_2 =
                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).count(3).horizontalSpread(0.05).verticalSpread(0.05).build();
    }
}
