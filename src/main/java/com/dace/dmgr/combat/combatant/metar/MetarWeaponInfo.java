package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.effect.TimedSoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class MetarWeaponInfo extends WeaponInfo<MetarWeapon> {
    /** 연사속도 */
    public static final FullAuto.FireRate FIRE_RATE = FullAuto.FireRate.RPM_840;
    /** 피해량 */
    public static final int DAMAGE = 30;
    /** 피해량 감소 시작 거리 (단위: 블록) */
    public static final int DAMAGE_WEAKENING_DISTANCE = 12;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 30;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 60;
    /** 탄퍼짐 */
    public static final double SPREAD = 4;
    /** 장탄수 */
    public static final int CAPACITY = 200;
    /** 재장전 시간 */
    public static final Timespan RELOAD_DURATION = Timespan.ofSeconds(3);
    /** 사용 시 이동속도 감소 시간 */
    public static final Timespan SLOW_DURATION = Timespan.ofSeconds(0.3);
    /** 사용 시 이동속도 감소량 */
    public static final int SLOW = 30;

    @Getter
    private static final MetarWeaponInfo instance = new MetarWeaponInfo();

    private MetarWeaponInfo() {
        super(MetarWeapon.class, RESOURCE.DEFAULT, "펄스 쌍기관포",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("대용량 탄창이 장착된 에너지 기관포입니다. 사격하여 <:DAMAGE:피해>를 입힙니다.")
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_WITH_DISTANCE,
                                DAMAGE, DAMAGE / 2, DAMAGE_WEAKENING_DISTANCE, DAMAGE_WEAKENING_DISTANCE * 2)
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME_WITH_RPM,
                                60.0 / FIRE_RATE.getRoundsPerMinute(), FIRE_RATE.getRoundsPerMinute())
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, CAPACITY)
                        .addActionKeyInfo("사격", ActionKey.RIGHT_CLICK)
                        .addActionKeyInfo("재장전", ActionKey.DROP)
                        .build()));
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static final class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 1561;
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(30, 255, 180)).build());
        /** 타격 */
        public static final ParticleEffect HIT = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT_MAGIC).count(6).horizontalSpread(0).verticalSpread(0).speed(0.1).build());
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_BREATH).volume(2.5).pitch(1.6).build(),
                SoundEffect.SoundInfo.builder("random.gun2.gatling_1").volume(2.5).pitch(0.9).build());
        /** 타격 */
        public static final SoundEffect HIT = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_POP).volume(0.6).pitch(0.55).pitchVariance(0.1).build());
        /** 재장전 */
        public static final TimedSoundEffect RELOAD = TimedSoundEffect.builder()
                .add(3, SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_HOWL).volume(0.6).pitch(0.5).build())
                .add(6, SoundEffect.SoundInfo.builder("new.block.beacon.deactivate").volume(0.6).pitch(1.7).build())
                .add(15, SoundEffect.SoundInfo.builder(Sound.ENTITY_VILLAGER_YES).volume(0.6).pitch(0.55).build())
                .add(19, SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_SHAKE).volume(0.6).pitch(0.5).build())
                .add(30, SoundEffect.SoundInfo.builder(Sound.BLOCK_PISTON_EXTEND).volume(0.6).pitch(0.8).build())
                .add(35, SoundEffect.SoundInfo.builder(Sound.BLOCK_IRON_TRAPDOOR_OPEN).volume(0.6).pitch(0.6).build())
                .add(40, SoundEffect.SoundInfo.builder(Sound.ENTITY_RABBIT_DEATH).volume(0.6).pitch(1.9).build())
                .add(42, SoundEffect.SoundInfo.builder("new.block.conduit.attack.target").volume(0.6).pitch(1.8).build())
                .add(45, SoundEffect.SoundInfo.builder(Sound.ENTITY_SKELETON_STEP).volume(0.6).pitch(0.5).build())
                .add(50, SoundEffect.SoundInfo.builder("new.block.beacon.activate").volume(0.6).pitch(1.7).build())
                .add(57, SoundEffect.SoundInfo.builder(Sound.BLOCK_IRON_TRAPDOOR_CLOSE).volume(0.6).pitch(0.65).build())
                .build();
    }
}
