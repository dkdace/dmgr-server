package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.util.DelayedDefinedSound;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public final class JagerWeaponInfo extends WeaponInfo<JagerWeaponL> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.25 * 20);
    /** 피해량 */
    public static final int DAMAGE = 100;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 30;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 100;
    /** 탄퍼짐 */
    public static final double SPREAD = 2.5;
    /** 달리기 탄퍼짐 배수 */
    public static final double SPREAD_SPRINT_MULTIPLIER = 2.5;
    /** 빙결량 */
    public static final int FREEZE = 15;
    /** 장탄수 */
    public static final int CAPACITY = 10;
    /** 재장전 시간 (tick) */
    public static final long RELOAD_DURATION = 2 * 20L;
    /** 무기 교체 시간 (tick) */
    public static final long SWAP_DURATION = (long) (0.25 * 20);
    /** 조준 시 이동속도 감소량 */
    public static final int AIM_SLOW = 30;
    @Getter
    private static final JagerWeaponInfo instance = new JagerWeaponInfo();

    private JagerWeaponInfo() {
        super(JagerWeaponL.class, RESOURCE.DEFAULT, "MK.73 ELNR",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("두 개의 탄창을 가진 특수 소총으로, <3::냉각탄> 및 정조준하여 <3::저격탄>을 사격할 수 있습니다.")
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, COOLDOWN / 20.0)
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
                                .build()
                        ),
                        new ActionInfoLore.NamedSection("저격탄", ActionInfoLore.Section
                                .builder("저격탄을 사격하여 <:DAMAGE:피해>를 입힙니다.")
                                .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_WITH_DISTANCE,
                                        SCOPE.DAMAGE, SCOPE.DAMAGE / 2, SCOPE.DAMAGE_WEAKENING_DISTANCE, SCOPE.DAMAGE_WEAKENING_DISTANCE * 2)
                                .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, SCOPE.CAPACITY)
                                .build()
                        )
                )
        );
    }

    /**
     * 정조준 상태의 정보.
     */
    @UtilityClass
    public static class SCOPE {
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
        public static class RECOIL {
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
    public static class RECOIL {
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
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 2;
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final DefinedSound USE = new DefinedSound(
                new DefinedSound.SoundEffect("random.gun2.m16_1", 0.8, 1.2),
                new DefinedSound.SoundEffect(Sound.BLOCK_FIRE_EXTINGUISH, 0.8, 1.7)
        );
        /** 전환 활성화 */
        public static final DefinedSound SWAP_ON = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_HOWL, 0.6, 1.9));
        /** 전환 비활성화 */
        public static final DefinedSound SWAP_OFF = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_SHAKE, 0.6, 1.9));
        /** 사용 (저격탄) */
        public static final DefinedSound USE_SCOPE = new DefinedSound(
                new DefinedSound.SoundEffect("random.gun2.psg_1_1", 3.5, 1),
                new DefinedSound.SoundEffect("random.gun2.m16_1", 3.5, 1),
                new DefinedSound.SoundEffect("random.gun_reverb", 5.5, 0.95)
        );
        /** 재장전 */
        public static final DelayedDefinedSound RELOAD = DelayedDefinedSound.builder()
                .add(3, new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_HOWL, 0.6, 1.7))
                .add(4, new DefinedSound.SoundEffect(Sound.BLOCK_FIRE_EXTINGUISH, 0.6, 1.2))
                .add(6, new DefinedSound.SoundEffect(Sound.ITEM_FLINTANDSTEEL_USE, 0.6, 0.8))
                .add(25, new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_HURT, 0.6, 0.5))
                .add(27, new DefinedSound.SoundEffect(Sound.ENTITY_CAT_PURREOW, 0.6, 1.7))
                .add(35, new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_SHAKE, 0.6, 1.8))
                .add(37, new DefinedSound.SoundEffect(Sound.BLOCK_IRON_DOOR_OPEN, 0.6, 1.7))
                .build();
    }
}
