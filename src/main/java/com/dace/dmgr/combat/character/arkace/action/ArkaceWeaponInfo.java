package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.util.DelayedDefinedSound;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class ArkaceWeaponInfo extends WeaponInfo<ArkaceWeapon> {
    /** 연사속도 */
    public static final FullAuto.FireRate FIRE_RATE = FullAuto.FireRate.RPM_600;
    /** 피해량 */
    public static final int DAMAGE = 75;
    /** 피해량 감소 시작 거리 (단위: 블록) */
    public static final int DAMAGE_WEAKENING_DISTANCE = 25;
    /** 장탄수 */
    public static final int CAPACITY = 30;
    /** 재장전 시간 (tick) */
    public static final long RELOAD_DURATION = (long) (1.5 * 20);
    /** 달리기 중 시전 시간 (tick) */
    public static final long SPRINT_READY_DURATION = (long) (0.25 * 20);
    @Getter
    private static final ArkaceWeaponInfo instance = new ArkaceWeaponInfo();

    private ArkaceWeaponInfo() {
        super(ArkaceWeapon.class, RESOURCE.DEFAULT, "HLN-12",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("뛰어난 안정성을 가진 전자동 돌격소총입니다. 사격하여 <:DAMAGE:피해>를 입힙니다.")
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_WITH_DISTANCE,
                                DAMAGE, DAMAGE / 2, DAMAGE_WEAKENING_DISTANCE, DAMAGE_WEAKENING_DISTANCE * 2)
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME_WITH_RPM,
                                60.0 / FIRE_RATE.getRoundsPerMinute(), FIRE_RATE.getRoundsPerMinute())
                        .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, CAPACITY)
                        .addActionKeyInfo("사격", ActionKey.RIGHT_CLICK)
                        .addActionKeyInfo("재장전", ActionKey.DROP)
                        .build()
                )
        );
    }

    /**
     * 반동 정보.
     */
    @UtilityClass
    public static class RECOIL {
        /** 수직 반동 */
        public static final double UP = 0.6;
        /** 수평 반동 */
        public static final double SIDE = 0.04;
        /** 수직 반동 분산도 */
        public static final double UP_SPREAD = 0.1;
        /** 수평 반동 분산도 */
        public static final double SIDE_SPREAD = 0.06;
    }

    /**
     * 탄퍼짐 정보.
     */
    @UtilityClass
    public static class SPREAD {
        /** 달리기 탄퍼짐 배수 */
        public static final double SPRINT_MULTIPLIER = 2;
        /** 탄퍼짐 증가량 */
        public static final double INCREMENT = 0.3;
        /** 탄퍼짐 시작 시점 */
        public static final int START = 5;
        /** 탄퍼짐 최대 시점 */
        public static final int MAX = 20;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 1;
        /** 달리기 */
        public static final short SPRINT = DEFAULT + 1000;
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final DefinedSound USE = new DefinedSound(
                new DefinedSound.SoundEffect("random.gun2.scarlight_1", 3, 1),
                new DefinedSound.SoundEffect("random.gun_reverb", 5, 1.2)
        );
        /** 사용 (궁극기) */
        public static final DefinedSound USE_ULT = new DefinedSound(
                new DefinedSound.SoundEffect("new.block.beacon.deactivate", 4, 2),
                new DefinedSound.SoundEffect("random.energy", 4, 1.6),
                new DefinedSound.SoundEffect("random.gun_reverb", 5, 1.2)
        );
        /** 재장전 */
        public static final DelayedDefinedSound RELOAD = DelayedDefinedSound.builder()
                .add(3, new DefinedSound.SoundEffect(Sound.BLOCK_PISTON_CONTRACT, 0.6, 1.6))
                .add(4, new DefinedSound.SoundEffect(Sound.ENTITY_VILLAGER_NO, 0.6, 1.9))
                .add(18, new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_HURT, 0.6, 0.5))
                .add(19, new DefinedSound.SoundEffect(Sound.ITEM_FLINTANDSTEEL_USE, 0.6, 1))
                .add(20, new DefinedSound.SoundEffect(Sound.ENTITY_VILLAGER_YES, 0.6, 1.8))
                .add(26, new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_SHAKE, 0.6, 1.7))
                .add(27, new DefinedSound.SoundEffect(Sound.BLOCK_IRON_DOOR_OPEN, 0.6, 1.8))
                .build();
    }
}
