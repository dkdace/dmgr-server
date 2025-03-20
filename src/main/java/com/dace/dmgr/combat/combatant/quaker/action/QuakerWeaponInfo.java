package com.dace.dmgr.combat.combatant.quaker.action;

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

public final class QuakerWeaponInfo extends WeaponInfo<QuakerWeapon> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(1.1);
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(0.3);
    /** 피해량 */
    public static final int DAMAGE = 300;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 3.5;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 0.5;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.3;

    @Getter
    private static final QuakerWeaponInfo instance = new QuakerWeaponInfo();

    private QuakerWeaponInfo() {
        super(QuakerWeapon.class, RESOURCE.DEFAULT, "타바르진",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("거대한 도끼를 휘둘러 근거리에 <:DAMAGE:광역 피해>를 입히고 옆으로 <:KNOCKBACK:밀쳐냅니다>.")
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
        public static final short DEFAULT = 3;
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_IRONGOLEM_ATTACK).volume(1).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder("random.gun2.shovel_leftclick").volume(1).pitch(0.6).pitchVariance(0.1).build());
        /** 타격 */
        public static final SoundEffect HIT = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_STRONG).volume(0.8).pitch(0.75).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR).volume(0.6).pitch(0.85).pitchVariance(0.1).build());
        /** 엔티티 타격 */
        public static final SoundEffect HIT_ENTITY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_STRONG).volume(1).pitch(0.9).pitchVariance(0.05).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_CRIT).volume(1).pitch(1.2).pitchVariance(0.1).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 (중심) */
        public static final ParticleEffect BULLET_TRAIL_CORE = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 200, 200, 200)
                        .count(12).horizontalSpread(0.3).verticalSpread(0.3).build());
        /** 총알 궤적 (장식) */
        public static final ParticleEffect BULLET_TRAIL_DECO = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(30).horizontalSpread(0.15).verticalSpread(0.15).speed(0.05).build());
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(20).speed(0.4).build());
    }
}
