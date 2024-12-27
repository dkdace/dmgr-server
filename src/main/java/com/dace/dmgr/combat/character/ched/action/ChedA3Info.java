package com.dace.dmgr.combat.character.ched.action;

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

public final class ChedA3Info extends ActiveSkillInfo<ChedA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 24 * 20L;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.6 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 60;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 30;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 7;
    /** 탐지 시간 (tick) */
    public static final long DETECT_DURATION = 6 * 20L;

    /** 탐지 점수 */
    public static final int DETECT_SCORE = 5;
    /** 처치 점수 */
    public static final int KILL_SCORE = 10;
    /** 처치 점수 제한시간 (tick) */
    public static final long KILL_SCORE_TIME_LIMIT = 7 * 20L;
    @Getter
    private static final ChedA3Info instance = new ChedA3Info();

    private ChedA3Info() {
        super(ChedA3.class, "고스트 피닉스",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("벽을 관통하는 유령 불사조를 날려보내 범위에 닿은 적을 탐지하여 아군에게 표시합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, SIZE)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DETECT_DURATION / 20.0)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build()
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
                SoundEffect.SoundInfo.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(2).pitch(1.6).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.7).build()
        );
        /** 사용 준비 */
        public static final SoundEffect USE_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDERDRAGON_FLAP).volume(1.5).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_VEX_CHARGE).volume(1.5).pitch(1.3).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_VEX_AMBIENT).volume(1.5).pitch(1.7).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_VEX_AMBIENT).volume(1.5).pitch(1.5).build()
        );
        /** 틱 효과음 */
        public static final SoundEffect TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.entity.phantom.flap").volume(1).pitch(1.3).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 사용 시 틱 입자 효과 */
        public static final ParticleEffect USE_TICK = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.CRIT_MAGIC)
                        .speedMultiplier(-0.25).build(),
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.SMOKE_NORMAL)
                        .speedMultiplier(0.12).build()
        );
        /** 총알 궤적 (중심) */
        public static final ParticleEffect BULLET_TRAIL_CORE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT_MAGIC).count(20).horizontalSpread(0.28).verticalSpread(0.28).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.SPELL_MOB, 64, 160, 184)
                        .count(15).horizontalSpread(2.5).verticalSpread(1.5).build()
        );
        /** 총알 궤적 (모양) */
        public static final ParticleEffect BULLET_TRAIL_SHAPE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT_MAGIC).count(8)
                        .horizontalSpread(0, 0, 1)
                        .verticalSpread(1, 0, 1)
                        .build());
    }
}
