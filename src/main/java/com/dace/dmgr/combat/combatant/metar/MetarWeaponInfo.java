package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.WeaponInfo;
import com.dace.dmgr.combat.ability.weapon.FullAuto;
import com.dace.dmgr.combat.entity.DistantDamage;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class MetarWeaponInfo extends WeaponInfo<MetarWeapon> {
    /** 연사속도 */
    public static final FullAuto.FireRate FIRE_RATE = FullAuto.FireRate.RPM_840;
    /** 피해량 */
    public static final int DAMAGE = 25;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 30;
    /** 거리별 피해량 */
    public static final DistantDamage DISTANT_DAMAGE = new DistantDamage(DAMAGE, DISTANCE / 2.0);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 60;
    /** 탄퍼짐 */
    public static final double SPREAD = 5;
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
        super(MetarWeapon.class, Resource.DEFAULT, "펄스 쌍기관포",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("대용량 탄창이 장착된 에너지 기관포입니다. 사격하여 <:DAMAGE:피해>를 입힙니다.")
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_WITH_DISTANCE,
                                DAMAGE, DAMAGE / 2, DISTANCE / 2, DISTANCE)
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
    public static final class Resource {
        /** 기본 */
        public static final short DEFAULT = 1561;
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_PLAYER_BREATH).volume(2.5).pitch(1.6).build(),
                SoundEffect.builder("random.gun2.gatling_1").volume(2.5).pitch(0.9).build());
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, Color.fromRGB(30, 255, 180)).build();
        /** 타격 */
        public static final PlayableEffect HIT = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_LAVA_POP).volume(0.6).pitch(0.55).pitchVariance(0.1).build(),

                ParticleEffect.Normal.builder(Particle.CRIT_MAGIC).count(6).speed(0.1).build());
        /** 재장전 */
        public static final PlayableEffect.Function<Long> RELOAD = i -> {
            switch (i.intValue()) {
                case 3:
                    return SoundEffect.builder(Sound.ENTITY_WOLF_HOWL).volume(0.6).pitch(0.5).build();
                case 6:
                    return SoundEffect.builder("new.block.beacon.deactivate").volume(0.6).pitch(1.7).build();
                case 15:
                    return SoundEffect.builder(Sound.ENTITY_VILLAGER_YES).volume(0.6).pitch(0.55).build();
                case 19:
                    return SoundEffect.builder(Sound.ENTITY_WOLF_SHAKE).volume(0.6).pitch(0.5).build();
                case 30:
                    return SoundEffect.builder(Sound.BLOCK_PISTON_EXTEND).volume(0.6).pitch(0.8).build();
                case 35:
                    return SoundEffect.builder(Sound.BLOCK_IRON_TRAPDOOR_OPEN).volume(0.6).pitch(0.6).build();
                case 40:
                    return SoundEffect.builder(Sound.ENTITY_RABBIT_DEATH).volume(0.6).pitch(1.9).build();
                case 42:
                    return SoundEffect.builder("new.block.conduit.attack.target").volume(0.6).pitch(1.8).build();
                case 45:
                    return SoundEffect.builder(Sound.ENTITY_SKELETON_STEP).volume(0.6).pitch(0.5).build();
                case 50:
                    return SoundEffect.builder("new.block.beacon.activate").volume(0.6).pitch(1.7).build();
                case 57:
                    return SoundEffect.builder(Sound.BLOCK_IRON_TRAPDOOR_CLOSE).volume(0.6).pitch(0.65).build();
                default:
                    return PlayableEffect.NONE;
            }
        };
    }
}
