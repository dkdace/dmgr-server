package com.dace.dmgr.combat.combatant.arkace.action;

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

public final class ArkaceA1Info extends ActiveSkillInfo<ArkaceA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 7 * 20L;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.5 * 20);
    /** 피해량 (폭발) */
    public static final int DAMAGE_EXPLODE = 120;
    /** 피해량 (직격) */
    public static final int DAMAGE_DIRECT = 40;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 60;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 3;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.25;

    /** 직격 점수 */
    public static final int DIRECT_HIT_SCORE = 3;
    @Getter
    private static final ArkaceA1Info instance = new ArkaceA1Info();

    private ArkaceA1Info() {
        super(ArkaceA1.class, "D.I.A. 코어 미사일",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("소형 미사일을 연속으로 발사하여 <:DAMAGE:광역 피해>를 입힙니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE + " (폭발)", DAMAGE_EXPLODE, DAMAGE_EXPLODE / 2)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE_DIRECT + " (직격)")
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2, ActionKey.LEFT_CLICK)
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
                SoundEffect.SoundInfo.builder("random.gun.grenade").volume(3).pitch(1.5).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_SHULKER_SHOOT).volume(3).pitch(1.2).build()
        );
        /** 폭발 */
        public static final SoundEffect EXPLODE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_FIREWORK_LARGE_BLAST).volume(4).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(4).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder("random.gun_reverb2").volume(6).pitch(0.9).build()
        );
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT_MAGIC).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 32, 250, 225).build()
        );
        /** 폭발 */
        public static final ParticleEffect EXPLODE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_NORMAL).count(40).horizontalSpread(0.2).verticalSpread(0.2).speed(0.2).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 32, 250, 225)
                        .count(200).horizontalSpread(2.5).verticalSpread(2.5).build()
        );
    }
}
