package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.effect.TimedSoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class PalasWeaponInfo extends WeaponInfo<PalasWeapon> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(0.4);
    /** 사용 후 쿨타임 */
    public static final Timespan ACTION_COOLDOWN = Timespan.ofSeconds(0.4);
    /** 피해량 */
    public static final int DAMAGE = 300;
    /** 치유량 */
    public static final int HEAL = 300;
    /** 치유 투사체 크기 (단위: 블록) */
    public static final double HEAL_SIZE = 0.2;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 30;
    /** 장탄수 */
    public static final int CAPACITY = 10;
    /** 재장전 시간 */
    public static final Timespan RELOAD_DURATION = Timespan.ofSeconds(2.2);
    /** 조준 시간 */
    public static final Timespan AIM_DURATION = Timespan.ofSeconds(0.25);
    /** 조준 시 이동속도 감소량 */
    public static final int AIM_SLOW = 30;
    /** 확대 레벨 */
    public static final Aimable.ZoomLevel ZOOM_LEVEL = Aimable.ZoomLevel.L3;

    @Getter
    private static final PalasWeaponInfo instance = new PalasWeaponInfo();

    private PalasWeaponInfo() {
        super(PalasWeapon.class, Resource.DEFAULT, "RQ-07",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("생체탄을 발사하는 볼트액션 소총입니다. " +
                                "사격하여 아군을 <:HEAL:치유>하거나 적에게 <:DAMAGE:피해>를 입힙니다. " +
                                "정조준 시 사거리 제한이 사라집니다.")
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, ChatColor.WHITE, COOLDOWN.plus(ACTION_COOLDOWN).toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.HEAL, HEAL)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, CAPACITY)
                        .addActionKeyInfo("사격", ActionKey.LEFT_CLICK)
                        .addActionKeyInfo("정조준", ActionKey.RIGHT_CLICK)
                        .addActionKeyInfo("재장전", ActionKey.DROP)
                        .build()));
    }

    /**
     * 반동 정보.
     */
    @UtilityClass
    public static final class Recoil {
        /** 수직 반동 */
        public static final double UP = 2.5;
        /** 수평 반동 */
        public static final double SIDE = 0;
        /** 수직 반동 분산도 */
        public static final double UP_SPREAD = 0.15;
        /** 수평 반동 분산도 */
        public static final double SIDE_SPREAD = 0.2;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static final class Resource {
        /** 기본 */
        public static final short DEFAULT = 15;
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.gun.vssvintorez").volume(2.5).pitch(1.3).build(),
                SoundEffect.SoundInfo.builder("random.gun2.qbz_95_1").volume(2.5).pitch(0.9).build(),
                SoundEffect.SoundInfo.builder("random.gun_reverb").volume(3.5).pitch(1.1).build());
        /** 조준 활성화 */
        public static final SoundEffect AIM_ON = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_HOWL).volume(0.4).pitch(2).build());
        /** 조준 비활성화 */
        public static final SoundEffect AIM_OFF = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_SHAKE).volume(0.4).pitch(2).build());
        /** 사용 후 쿨타임 */
        public static final TimedSoundEffect ACTION = TimedSoundEffect.builder()
                .add(1, SoundEffect.SoundInfo.builder(Sound.ENTITY_VILLAGER_YES).volume(0.6).pitch(1.2).build())
                .add(3, SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(0.5).pitch(1.4).build())
                .add(5, SoundEffect.SoundInfo.builder(Sound.ENTITY_VILLAGER_NO).volume(0.6).pitch(1.2).build())
                .build();
        /** 재장전 */
        public static final TimedSoundEffect RELOAD = TimedSoundEffect.builder()
                .add(3, SoundEffect.SoundInfo.builder("new.ui.stonecutter.take_result").volume(0.6).pitch(1.5).build())
                .add(6, SoundEffect.SoundInfo.builder(Sound.BLOCK_PISTON_CONTRACT).volume(0.6).pitch(1.3).build())
                .add(10, SoundEffect.SoundInfo.builder(Sound.ITEM_FLINTANDSTEEL_USE).volume(0.6).pitch(0.8).build())
                .add(12, SoundEffect.SoundInfo.builder(Sound.ITEM_BOTTLE_EMPTY).volume(0.6).pitch(1.4).build())
                .add(14, SoundEffect.SoundInfo.builder(Sound.BLOCK_IRON_TRAPDOOR_OPEN).volume(0.6).pitch(1.3).build())
                .add(22, SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_HURT).volume(0.6).pitch(0.5).build())
                .add(24, SoundEffect.SoundInfo.builder(Sound.ENTITY_CAT_PURREOW).volume(0.6).pitch(1.8).build())
                .add(32, SoundEffect.SoundInfo.builder(Sound.ITEM_BOTTLE_FILL).volume(0.6).pitch(1.4).build())
                .add(38, SoundEffect.SoundInfo.builder("new.block.chain.place").volume(0.6).pitch(1.6).build())
                .add(41, SoundEffect.SoundInfo.builder(Sound.BLOCK_IRON_TRAPDOOR_CLOSE).volume(0.6).pitch(1.2).build())
                .build();
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class Particles {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(210, 160, 70)).build());
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.WATER_SPLASH).count(15).build());
    }
}
