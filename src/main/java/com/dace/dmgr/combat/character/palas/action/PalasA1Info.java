package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.util.ParticleEffect;
import com.dace.dmgr.util.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class PalasA1Info extends ActiveSkillInfo<PalasA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 12 * 20L;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (1.2 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 10;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 50;
    /** 기절 시간 (tick) */
    public static final long STUN_DURATION = (long) (1.8 * 20);

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 8;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;
    @Getter
    private static final PalasA1Info instance = new PalasA1Info();

    private PalasA1Info() {
        super(PalasA1.class, "테이저건",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("테이저건을 발사하여 약간의 <:DAMAGE:피해>를 입히고 <:STUN:기절>시킵니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.STUN, Format.TIME, STUN_DURATION / 20.0)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
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
                SoundEffect.SoundInfo.builder(Sound.ENTITY_CAT_PURREOW).volume(0.5).pitch(1.6).build());
        /** 사용 준비 */
        public static final SoundEffect USE_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ARROW_SHOOT).volume(1.5).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder("random.gun.m1911_silencer").volume(1.5).pitch(0.8).build()
        );
        /** 엔티티 타격 */
        public static final SoundEffect HIT_ENTITY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_FIREWORK_TWINKLE).volume(2).pitch(1.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_FIREWORK_BLAST).volume(2).pitch(1.6).build(),
                SoundEffect.SoundInfo.builder("random.stab").volume(2).pitch(2).build()
        );
        /** 틱 효과음 */
        public static final SoundEffect TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_FIREWORK_BLAST).volume(2).pitch(1.6).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_FIREWORK_BLAST).volume(2).pitch(1.8).build()
        );
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 240, 230, 50)
                        .build()
        );
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 240, 230, 50)
                        .count(20)
                        .horizontalSpread(0, 0, 0.5)
                        .verticalSpread(1, 0, 0.5)
                        .build());
        /** 틱 입자 효과 */
        public static final ParticleEffect TICK = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(20).speed(0.6).build());
    }
}
