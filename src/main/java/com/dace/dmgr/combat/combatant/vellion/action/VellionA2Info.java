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
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class VellionA2Info extends ActiveSkillInfo<VellionA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(5);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.8);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 초당 피해량 */
    public static final int DAMAGE_PER_SECOND = 60;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 8;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 20;
    /** 방어력 감소량 */
    public static final int DEFENSE_DECREMENT = 25;
    /** 대상 위치 통과 불가 시 초기화 제한 시간 */
    public static final Timespan BLOCK_RESET_DELAY = Timespan.ofSeconds(2);

    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;
    @Getter
    private static final VellionA2Info instance = new VellionA2Info();

    private VellionA2Info() {
        super(VellionA2.class, "저주 귀속",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("바라보는 적에게 저주를 걸어 <:DEFENSE_DECREASE:방어력>을 감소시키고 해당 적을 제외한 주변에 지속적인 <:DAMAGE:광역 피해>를 입힙니다. " +
                                "해당 적이 시야에서 " + BLOCK_RESET_DELAY.toSeconds() + "초간 사라지거나 사거리를 벗어나면 저주가 풀립니다.")
                        .addValueInfo(TextIcon.DEFENSE_DECREASE, Format.PERCENT, DEFENSE_DECREMENT)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addValueInfo(TextIcon.DAMAGE, Format.PER_SECOND, DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build(),
                        new ActionInfoLore.NamedSection("취소/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("해제", ActionKey.SLOT_2)
                                .build()
                        )
                )
        );
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder("new.entity.squid.squirt").volume(2).pitch(1.2).build()
        );
        /** 사용 준비 */
        public static final SoundEffect USE_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ELDER_GUARDIAN_CURSE).volume(2).pitch(1).build(),
                SoundEffect.SoundInfo.builder("new.block.respawn_anchor.charge").volume(2).pitch(0.8).build()
        );
        /** 발동 */
        public static final SoundEffect TRIGGER = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_END_PORTAL_FRAME_FILL).volume(0.6).pitch(0.7).pitchVariance(0.1).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 사용 시 틱 입자 효과 - 1 */
        public static final ParticleEffect USE_TICK_1 = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(0, ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        200, 140, 130, 130, 230, 155).build());
        /** 사용 시 틱 입자 효과 - 2 */
        public static final ParticleEffect USE_TICK_2 = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.SPELL_WITCH).build());
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(0, ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        200, 140, 130, 130, 230, 155).build());
        /** 표식 */
        public static final ParticleEffect MARK = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 160, 150, 152)
                        .count(4).horizontalSpread(0.2).verticalSpread(0.2).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.SPELL_MOB, 160, 150, 152).build()
        );
        /** 엔티티 타격 (표식 - 중심) */
        public static final ParticleEffect HIT_ENTITY_MARK_CORE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT_MAGIC).count(15).speed(0.3).build());
        /** 엔티티 타격 (표식 - 장식) */
        public static final ParticleEffect HIT_ENTITY_MARK_DECO = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_NORMAL).build());
    }
}
