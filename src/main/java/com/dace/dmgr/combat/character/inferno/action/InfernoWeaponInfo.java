package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.effect.TimedSoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class InfernoWeaponInfo extends WeaponInfo<InfernoWeapon> {
    /** 초당 피해량 */
    public static final int DAMAGE_PER_SECOND = 150;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 40;
    /** 화염 지속 시간 (tick) */
    public static final long FIRE_DURATION = (long) (2.5 * 20);
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
    /** 재장전 시간 (tick) */
    public static final long RELOAD_DURATION = (long) (2.5 * 20);
    @Getter
    private static final InfernoWeaponInfo instance = new InfernoWeaponInfo();

    private InfernoWeaponInfo() {
        super(InfernoWeapon.class, RESOURCE.DEFAULT, "파이어스톰",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("근거리에 화염을 흩뿌리거나 화염탄을 발사할 수 있는 화염방사기입니다.")
                        .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, CAPACITY)
                        .addActionKeyInfo("방사", ActionKey.RIGHT_CLICK)
                        .addActionKeyInfo("화염탄", ActionKey.LEFT_CLICK)
                        .build(),
                        new ActionInfoLore.NamedSection("방사", ActionInfoLore.Section
                                .builder("근거리에 화염을 방사하여 <:DAMAGE:광역 피해>와 <:FIRE:화염 피해>를 입힙니다.")
                                .addValueInfo(TextIcon.DAMAGE, Format.PER_SECOND, DAMAGE_PER_SECOND)
                                .addValueInfo(TextIcon.FIRE, Format.TIME_WITH_PER_SECOND, FIRE_DURATION / 20.0, FIRE_DAMAGE_PER_SECOND)
                                .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                                .build()
                        ),
                        new ActionInfoLore.NamedSection("화염탄", ActionInfoLore.Section
                                .builder("폭발하는 화염 구체를 발사하여 <:DAMAGE:광역 피해>와 <:FIRE:화염 피해>를 입힙니다.")
                                .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE + " (폭발)", FIREBALL.DAMAGE_EXPLODE, FIREBALL.DAMAGE_EXPLODE / 2)
                                .addValueInfo(TextIcon.DAMAGE, FIREBALL.DAMAGE_DIRECT + " (직격)")
                                .addValueInfo(TextIcon.FIRE, Format.VARIABLE_TIME_WITH_PER_SECOND,
                                        FIRE_DURATION / 20.0, FIRE_DURATION / 2 / 20.0, FIRE_DAMAGE_PER_SECOND)
                                .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, FIREBALL.COOLDOWN / 20.0)
                                .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, FIREBALL.DISTANCE)
                                .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, FIREBALL.RADIUS)
                                .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, -FIREBALL.CAPACITY_CONSUME)
                                .build()
                        )
                )
        );
    }

    /**
     * 화염탄의 정보.
     */
    @UtilityClass
    public static class FIREBALL {
        /** 쿨타임 (tick) */
        public static final long COOLDOWN = 20;
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
        /** 탄환 소모량 */
        public static final int CAPACITY_CONSUME = 50;
        /** 넉백 강도 */
        public static final double KNOCKBACK = 0.2;

        /**
         * 반동 정보.
         */
        @UtilityClass
        public static class RECOIL {
            /** 수직 반동 */
            public static final double UP = 5.0;
            /** 수평 반동 */
            public static final double SIDE = 0;
            /** 수직 반동 분산도 */
            public static final double UP_SPREAD = 1.0;
            /** 수평 반동 분산도 */
            public static final double SIDE_SPREAD = 0.8;
        }
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 12;
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_HORSE_BREATHE).volume(1.5).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_HORSE_BREATHE).volume(1.5).pitch(1.3).build(),
                SoundEffect.SoundInfo.builder("new.block.soul_sand.fall").volume(1.5).pitch(0.5).build()
        );
        /** 사용 (화염탄) */
        public static final SoundEffect USE_FIREBALL = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_SHULKER_SHOOT).volume(2).pitch(1.5).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GHAST_SHOOT).volume(2).pitch(1.1).build(),
                SoundEffect.SoundInfo.builder("random.gun.grenade").volume(2).pitch(0.9).build()
        );
        /** 폭발 (화염탄) */
        public static final SoundEffect FIREBALL_EXPLODE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(3).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(3).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder("random.gun_reverb2").volume(5).pitch(1).build()
        );
        /** 재장전 */
        public static final TimedSoundEffect RELOAD = TimedSoundEffect.builder()
                .add(3, SoundEffect.SoundInfo.builder(Sound.ENTITY_VILLAGER_YES).volume(0.6).pitch(0.5).build())
                .add(6, SoundEffect.SoundInfo.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(0.6).pitch(0.5).build())
                .add(10, SoundEffect.SoundInfo.builder(Sound.BLOCK_PISTON_EXTEND).volume(0.6).pitch(0.7).build())
                .add(27, SoundEffect.SoundInfo.builder(Sound.ENTITY_VILLAGER_NO).volume(0.6).pitch(0.5).build())
                .add(30, SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_SHAKE).volume(0.6).pitch(0.5).build())
                .add(44, SoundEffect.SoundInfo.builder(Sound.BLOCK_IRON_DOOR_OPEN).volume(0.6).pitch(0.7).build())
                .add(47, SoundEffect.SoundInfo.builder(Sound.ENTITY_IRONGOLEM_ATTACK).volume(0.6).pitch(1.4).build())
                .build();
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.FLAME)
                        .speedMultiplier(1, 1.3, 0.8)
                        .build(),
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.SMOKE_NORMAL)
                        .speedMultiplier(1.45).build()
        );
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_NORMAL).count(3).horizontalSpread(0.2).verticalSpread(0.2).speed(0.05).build());
        /** 블록 타격 */
        public static final ParticleEffect HIT_BLOCK = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.DRIP_LAVA).count(2).horizontalSpread(0.07).verticalSpread(0.07).build());
        /** 총알 궤적 (화염탄) */
        public static final ParticleEffect BULLET_TRAIL_FIREBALL = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.FLAME).count(10).horizontalSpread(0.12).verticalSpread(0.12).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_NORMAL).count(13).horizontalSpread(0.15).verticalSpread(0.15).speed(0.04).build()
        );
        /** 폭발 (화염탄) */
        public static final ParticleEffect FIREBALL_EXPLODE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_LARGE).count(40).horizontalSpread(0.2).verticalSpread(0.2).speed(0.1).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_NORMAL).count(80).horizontalSpread(0.1).verticalSpread(0.1).speed(0.15).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.LAVA).count(30).horizontalSpread(0.3).verticalSpread(0.3).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.FLAME).count(80).horizontalSpread(0.2).verticalSpread(0.2).speed(0.1).build()
        );
    }
}
