package com.dace.dmgr.combat.combatant.silia.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class SiliaWeaponInfo extends WeaponInfo<SiliaWeapon> {
    /** 피해량 */
    public static final int DAMAGE = 200;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 12;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 35;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.4;
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(0.9);

    @Getter
    private static final SiliaWeaponInfo instance = new SiliaWeaponInfo();

    private SiliaWeaponInfo() {
        super(SiliaWeapon.class, RESOURCE.DEFAULT, "접이식 마체테",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("휴대성이 뛰어난 접이식 마체테입니다. " +
                                "검기를 날려 <:DAMAGE:피해>를 입힙니다.")
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.LEFT_CLICK)
                        .build()));
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static final class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 4;
        /** 확장 */
        public static final short EXTENDED = DEFAULT + 1000;
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.gun2.knife_leftclick").volume(0.8).pitch(1).build(),
                SoundEffect.SoundInfo.builder("random.swordhit").volume(0.7).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder("new.item.trident.riptide_1").volume(0.6).pitch(1.3).build());
        /** 엔티티 타격 */
        public static final SoundEffect HIT_ENTITY = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.stab").volume(1).pitch(0.8).pitchVariance(0.05).build());
        /** 블록 타격 */
        public static final SoundEffect HIT_BLOCK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_WEAK).volume(1).pitch(0.9).pitchVariance(0.05).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 255, 255, 255)
                        .count(2).horizontalSpread(0.05).verticalSpread(0.05).build());
        /** 타격 */
        public static final ParticleEffect HIT = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_NORMAL).count(10).horizontalSpread(0.1).verticalSpread(0.1).speed(0.15)
                        .build());
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(15).speed(0.4).build());
    }
}
