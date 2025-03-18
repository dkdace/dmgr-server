package com.dace.dmgr.combat.combatant.jager.action;

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
import org.bukkit.Sound;

public final class JagerWeaponInfo extends WeaponInfo<JagerWeaponL> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(0.25);
    /** 피해량 */
    public static final int DAMAGE = 70;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 30;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 100;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.2;
    /** 빙결량 */
    public static final int FREEZE = 15;
    /** 장탄수 */
    public static final int CAPACITY = 10;
    /** 재장전 시간 */
    public static final Timespan RELOAD_DURATION = Timespan.ofSeconds(2);
    /** 무기 교체 시간 */
    public static final Timespan SWAP_DURATION = Timespan.ofSeconds(0.25);
    /** 조준 시 이동속도 감소량 */
    public static final int AIM_SLOW = 30;

    @Getter
    private static final JagerWeaponInfo instance = new JagerWeaponInfo();

    private JagerWeaponInfo() {
        super(JagerWeaponL.class, RESOURCE.DEFAULT, "MK.73 ELNR",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("두 개의 탄창을 가진 특수 소총으로, <3::냉각탄> 및 정조준하여 <3::저격탄>을 사격할 수 있습니다.")
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, COOLDOWN.toSeconds())
                        .addActionKeyInfo("사격", ActionKey.LEFT_CLICK)
                        .addActionKeyInfo("정조준", ActionKey.RIGHT_CLICK)
                        .addActionKeyInfo("재장전", ActionKey.DROP)
                        .build(),
                        new ActionInfoLore.NamedSection("냉각탄", ActionInfoLore.Section
                                .builder("냉각탄을 사격하여 <:DAMAGE:피해>를 입히고 <5:WALK_SPEED_DECREASE:> <d::빙결>시킵니다.")
                                .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                                .addValueInfo(TextIcon.WALK_SPEED_DECREASE, ChatColor.DARK_PURPLE, FREEZE)
                                .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                                .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, CAPACITY)
                                .build()),
                        new ActionInfoLore.NamedSection("저격탄", ActionInfoLore.Section
                                .builder("저격탄을 사격하여 <:DAMAGE:피해>를 입힙니다.")
                                .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_WITH_DISTANCE,
                                        SCOPE.DAMAGE, SCOPE.DAMAGE / 2, SCOPE.DAMAGE_WEAKENING_DISTANCE, SCOPE.DAMAGE_WEAKENING_DISTANCE * 2)
                                .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, SCOPE.CAPACITY)
                                .build())));
    }

    /**
     * 정조준 상태의 정보.
     */
    @UtilityClass
    public static final class SCOPE {
        /** 피해량 */
        public static final int DAMAGE = 240;
        /** 피해량 감소 시작 거리 (단위: 블록) */
        public static final int DAMAGE_WEAKENING_DISTANCE = 30;
        /** 장탄수 */
        public static final int CAPACITY = 7;
        /** 확대 레벨 */
        public static final Aimable.ZoomLevel ZOOM_LEVEL = Aimable.ZoomLevel.L4;

        /**
         * 반동 정보.
         */
        @UtilityClass
        public static final class RECOIL {
            /** 수직 반동 */
            public static final double UP = 2.8;
            /** 수평 반동 */
            public static final double SIDE = 0;
            /** 수직 반동 분산도 */
            public static final double UP_SPREAD = 0.25;
            /** 수평 반동 분산도 */
            public static final double SIDE_SPREAD = 0.3;
        }
    }

    /**
     * 반동 정보.
     */
    @UtilityClass
    public static final class RECOIL {
        /** 수직 반동 */
        public static final double UP = 0.8;
        /** 수평 반동 */
        public static final double SIDE = 0;
        /** 수직 반동 분산도 */
        public static final double UP_SPREAD = 0.1;
        /** 수평 반동 분산도 */
        public static final double SIDE_SPREAD = 0.05;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static final class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 2;
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.gun2.m16_1").volume(0.8).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(0.8).pitch(1.7).build());
        /** 조준 활성화 */
        public static final SoundEffect AIM_ON = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_HOWL).volume(0.6).pitch(1.9).build());
        /** 조준 비활성화 */
        public static final SoundEffect AIM_OFF = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_SHAKE).volume(0.6).pitch(1.9).build());
        /** 사용 (저격탄) */
        public static final SoundEffect USE_SCOPE = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.gun2.psg_1_1").volume(3.5).pitch(1).build(),
                SoundEffect.SoundInfo.builder("random.gun2.m16_1").volume(3.5).pitch(1).build(),
                SoundEffect.SoundInfo.builder("random.gun_reverb").volume(5.5).pitch(0.95).build());
        /** 재장전 */
        public static final TimedSoundEffect RELOAD = TimedSoundEffect.builder()
                .add(3, SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_HOWL).volume(0.6).pitch(1.7).build())
                .add(4, SoundEffect.SoundInfo.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(0.6).pitch(1.2).build())
                .add(6, SoundEffect.SoundInfo.builder(Sound.ITEM_FLINTANDSTEEL_USE).volume(0.6).pitch(0.8).build())
                .add(25, SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_HURT).volume(0.6).pitch(0.5).build())
                .add(27, SoundEffect.SoundInfo.builder(Sound.ENTITY_CAT_PURREOW).volume(0.6).pitch(1.7).build())
                .add(35, SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_SHAKE).volume(0.6).pitch(1.8).build())
                .add(37, SoundEffect.SoundInfo.builder(Sound.BLOCK_IRON_DOOR_OPEN).volume(0.6).pitch(1.7).build())
                .build();
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 137, 185, 240)
                        .build());
        /** 타격 */
        public static final ParticleEffect HIT = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 137, 185, 240)
                        .count(10).horizontalSpread(0.25).verticalSpread(0.25).build());
    }
}
