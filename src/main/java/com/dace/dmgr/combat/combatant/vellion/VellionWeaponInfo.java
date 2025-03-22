package com.dace.dmgr.combat.combatant.vellion;

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
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class VellionWeaponInfo extends WeaponInfo<VellionWeapon> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(0.5);
    /** 피해량 */
    public static final int DAMAGE = 120;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 30;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 35;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.3;

    @Getter
    private static final VellionWeaponInfo instance = new VellionWeaponInfo();

    private VellionWeaponInfo() {
        super(VellionWeapon.class, RESOURCE.DEFAULT, "절멸",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("마법 구체를 발사하여 <:DAMAGE:피해>를 입힙니다.")
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
        public static final short DEFAULT = 14;
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDERDRAGON_HURT).volume(0.8).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_END_PORTAL_FRAME_FILL).volume(1).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_END_PORTAL_FRAME_FILL).volume(1).pitch(0.9).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.SPELL_WITCH).count(4).horizontalSpread(0.1).verticalSpread(0.1).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(80, 30, 110)).count(6).horizontalSpread(0.25).verticalSpread(0.25).build());
        /** 타격 */
        public static final ParticleEffect HIT = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_NORMAL).count(30).horizontalSpread(0.1).verticalSpread(0.1).speed(0.1).build());
    }
}
