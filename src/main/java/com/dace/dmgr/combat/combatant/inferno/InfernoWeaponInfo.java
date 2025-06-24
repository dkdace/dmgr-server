package com.dace.dmgr.combat.combatant.inferno;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.WeaponInfo;
import com.dace.dmgr.combat.entity.DistantDamage;
import com.dace.dmgr.combat.entity.DistantTimespan;
import com.dace.dmgr.combat.entity.combatuser.ScreenRecoil;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public final class InfernoWeaponInfo extends WeaponInfo<InfernoWeapon> {
    /** 초당 피해량 */
    public static final int DAMAGE_PER_SECOND = 150;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 40;
    /** 화염 지속 시간 */
    public static final Timespan FIRE_DURATION = Timespan.ofSeconds(2.5);
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 7;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 0.8;
    /** 탄퍼짐 */
    public static final double SPREAD = 30;
    /** 장탄수 */
    public static final int CAPACITY = 200;
    /** 재장전 시간 */
    public static final Timespan RELOAD_DURATION = Timespan.ofSeconds(2.5);

    @Getter
    private static final InfernoWeaponInfo instance = new InfernoWeaponInfo();

    private InfernoWeaponInfo() {
        super(InfernoWeapon.class, Resource.DEFAULT, "파이어스톰",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("근거리에 화염을 흩뿌리거나 화염탄을 발사할 수 있는 화염방사기입니다.")
                        .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, CAPACITY)
                        .addActionKeyInfo("방사", ActionKey.RIGHT_CLICK)
                        .addActionKeyInfo("화염탄", ActionKey.LEFT_CLICK)
                        .build(),
                        new AbilityInfoLore.NamedSection("방사", AbilityInfoLore.Section
                                .builder("근거리에 화염을 방사하여 <:DAMAGE:광역 피해>와 <:FIRE:화염 피해>를 입힙니다.")
                                .addValueInfo(TextIcon.DAMAGE, Format.PER_SECOND, DAMAGE_PER_SECOND)
                                .addValueInfo(TextIcon.FIRE, Format.TIME_WITH_PER_SECOND, FIRE_DURATION.toSeconds(), FIRE_DAMAGE_PER_SECOND)
                                .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                                .build()),
                        new AbilityInfoLore.NamedSection("화염탄", AbilityInfoLore.Section
                                .builder("폭발하는 화염 구체를 발사하여 <:DAMAGE:광역 피해>와 <:FIRE:화염 피해>를 입힙니다.")
                                .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE + " (폭발)", Fireball.DAMAGE_EXPLODE, Fireball.DAMAGE_EXPLODE / 2)
                                .addValueInfo(TextIcon.DAMAGE, Fireball.DAMAGE_DIRECT + " (직격)")
                                .addValueInfo(TextIcon.FIRE, Format.VARIABLE_TIME_WITH_PER_SECOND,
                                        FIRE_DURATION.toSeconds(), FIRE_DURATION.toSeconds() / 2, FIRE_DAMAGE_PER_SECOND)
                                .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, Fireball.COOLDOWN.toSeconds())
                                .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, Fireball.DISTANCE)
                                .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, Fireball.RADIUS)
                                .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, -Fireball.CAPACITY_CONSUME)
                                .build())));
    }

    /**
     * 화염탄의 정보.
     */
    @UtilityClass
    public static final class Fireball {
        /** 쿨타임 */
        public static final Timespan COOLDOWN = Timespan.ofSeconds(1);
        /** 피해량 (폭발) */
        public static final int DAMAGE_EXPLODE = 100;
        /** 피해량 (직격) */
        public static final int DAMAGE_DIRECT = 40;
        /** 사거리 (단위: 블록) */
        public static final int DISTANCE = 10;
        /** 투사체 속력 (단위: 블록/s) */
        public static final int VELOCITY = 30;
        /** 투사체 크기 (단위: 블록) */
        public static final double SIZE = 0.5;
        /** 피해 범위 (단위: 블록) */
        public static final double RADIUS = 2.5;
        /** 거리별 피해량 (폭발) */
        public static final DistantDamage DISTANT_DAMAGE_EXPLODE = new DistantDamage(DAMAGE_EXPLODE, RADIUS / 2);
        /** 거리별 화염 지속 시간 */
        public static final DistantTimespan DISTANT_FIRE_DURATION = new DistantTimespan(FIRE_DURATION, RADIUS / 2);
        /** 탄환 소모량 */
        public static final int CAPACITY_CONSUME = 50;
        /** 넉백 강도 */
        public static final double KNOCKBACK = 0.2;
        /** 반동 */
        public static final ScreenRecoil RECOIL = new ScreenRecoil(5, 0, 1, 0.8, Timespan.ofTicks(3), 1);
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static final class Resource {
        /** 기본 */
        public static final short DEFAULT = 12;
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_HORSE_BREATHE).volume(1.5).pitch(0.7).build(),
                SoundEffect.builder(Sound.ENTITY_HORSE_BREATHE).volume(1.5).pitch(1.3).build(),
                SoundEffect.builder("new.block.soul_sand.fall").volume(1.5).pitch(0.5).build());
        /** 총알 궤적 */
        public static final PlayableEffect.BiFunction<Vector, Double> BULLET_TRAIL = (velocity, distance) -> PlayableEffect.list(
                ParticleEffect.Directional.create(Particle.FLAME, velocity.clone().multiply(1.3 - distance * 0.1)),
                ParticleEffect.Directional.create(Particle.SMOKE_NORMAL, velocity.clone().multiply(1.45)));
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY =
                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).count(3).horizontalSpread(0.2).verticalSpread(0.2).speed(0.05).build();
        /** 블록 타격 */
        public static final ParticleEffect HIT_BLOCK =
                ParticleEffect.Normal.builder(Particle.DRIP_LAVA).count(2).horizontalSpread(0.07).verticalSpread(0.07).build();
        /** 화염탄 - 사용 */
        public static final PlayableEffect FIREBALL_USE = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_SHULKER_SHOOT).volume(2).pitch(1.5).build(),
                SoundEffect.builder(Sound.ENTITY_GHAST_SHOOT).volume(2).pitch(1.1).build(),
                SoundEffect.builder("random.gun.grenade").volume(2).pitch(0.9).build());
        /** 화염탄 - 총알 궤적 */
        public static final PlayableEffect FIREBALL_BULLET_TRAIL = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.FLAME).count(10).horizontalSpread(0.12).verticalSpread(0.12).build(),
                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).count(13).horizontalSpread(0.15).verticalSpread(0.15).speed(0.04).build());
        /** 화염탄 - 폭발 */
        public static final PlayableEffect FIREBALL_EXPLODE = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(3).pitch(0.8).build(),
                SoundEffect.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(3).pitch(1.4).build(),
                SoundEffect.builder("random.gun_reverb2").volume(5).pitch(1).build(),

                ParticleEffect.Normal.builder(Particle.SMOKE_LARGE).count(40).horizontalSpread(0.2).verticalSpread(0.2).speed(0.1).build(),
                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).count(80).horizontalSpread(0.1).verticalSpread(0.1).speed(0.15).build(),
                ParticleEffect.Normal.builder(Particle.LAVA).count(30).horizontalSpread(0.3).verticalSpread(0.3).build(),
                ParticleEffect.Normal.builder(Particle.FLAME).count(80).horizontalSpread(0.2).verticalSpread(0.2).speed(0.1).build());
        /** 재장전 */
        public static final PlayableEffect.Function<Long> RELOAD = i -> {
            switch (i.intValue()) {
                case 3:
                    return SoundEffect.builder(Sound.ENTITY_VILLAGER_YES).volume(0.6).pitch(0.5).build();
                case 6:
                    return SoundEffect.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(0.6).pitch(0.5).build();
                case 10:
                    return SoundEffect.builder(Sound.BLOCK_PISTON_EXTEND).volume(0.6).pitch(0.7).build();
                case 27:
                    return SoundEffect.builder(Sound.ENTITY_VILLAGER_NO).volume(0.6).pitch(0.5).build();
                case 30:
                    return SoundEffect.builder(Sound.ENTITY_WOLF_SHAKE).volume(0.6).pitch(0.5).build();
                case 44:
                    return SoundEffect.builder(Sound.BLOCK_IRON_DOOR_OPEN).volume(0.6).pitch(0.7).build();
                case 47:
                    return SoundEffect.builder(Sound.ENTITY_IRONGOLEM_ATTACK).volume(0.6).pitch(1.4).build();
                default:
                    return PlayableEffect.NONE;
            }
        };
    }
}
