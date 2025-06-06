package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.entity.DistantDamage;
import com.dace.dmgr.combat.entity.combatuser.ScreenRecoil;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
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
    /** 거리별 피해량 */
    public static final DistantDamage DISTANT_DAMAGE = new DistantDamage(DAMAGE, DAMAGE_WEAKENING_DISTANCE);
    /** 장탄수 */
    public static final int CAPACITY = 30;
    /** 재장전 시간 */
    public static final Timespan RELOAD_DURATION = Timespan.ofSeconds(1.5);
    /** 달리기 중 시전 시간 */
    public static final Timespan SPRINT_READY_DURATION = Timespan.ofSeconds(0.25);
    /** 반동 */
    public static final ScreenRecoil RECOIL = new ScreenRecoil(0.6, 0.04, 0.1, 0.06, Timespan.ofTicks(2), 2);

    @Getter
    private static final ArkaceWeaponInfo instance = new ArkaceWeaponInfo();

    private ArkaceWeaponInfo() {
        super(ArkaceWeapon.class, Resource.DEFAULT, "HLN-12",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("뛰어난 안정성을 가진 전자동 돌격소총입니다. 사격하여 <:DAMAGE:피해>를 입힙니다.")
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_WITH_DISTANCE,
                                DAMAGE, DAMAGE / 2, DAMAGE_WEAKENING_DISTANCE, DAMAGE_WEAKENING_DISTANCE * 2)
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME_WITH_RPM,
                                60.0 / FIRE_RATE.getRoundsPerMinute(), FIRE_RATE.getRoundsPerMinute())
                        .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, CAPACITY)
                        .addActionKeyInfo("사격", ActionKey.RIGHT_CLICK)
                        .addActionKeyInfo("재장전", ActionKey.DROP)
                        .build()));
    }

    /**
     * 탄퍼짐 정보.
     */
    @UtilityClass
    public static final class Spread {
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
    public static final class Resource {
        /** 기본 */
        public static final short DEFAULT = 1;
        /** 달리기 */
        public static final short SPRINT = DEFAULT + 1000;
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder("random.gun2.scarlight_1").volume(3).pitch(1).build(),
                SoundEffect.builder("random.gun_reverb").volume(5).pitch(1.2).build());
        /** 탄피 효과음 */
        public static final PlayableEffect SHELL_DROP =
                CombatEffectUtil.SHELL_DROP_SOUND.apply(1.0);
        /** 재장전 */
        public static final PlayableEffect.Function<Long> RELOAD = i -> {
            switch (i.intValue()) {
                case 3:
                    return SoundEffect.builder(Sound.BLOCK_PISTON_CONTRACT).volume(0.6).pitch(1.6).build();
                case 4:
                    return SoundEffect.builder(Sound.ENTITY_VILLAGER_NO).volume(0.6).pitch(1.9).build();
                case 18:
                    return SoundEffect.builder(Sound.ENTITY_PLAYER_HURT).volume(0.6).pitch(0.5).build();
                case 19:
                    return SoundEffect.builder(Sound.ITEM_FLINTANDSTEEL_USE).volume(0.6).pitch(1).build();
                case 20:
                    return SoundEffect.builder(Sound.ENTITY_VILLAGER_YES).volume(0.6).pitch(1.8).build();
                case 26:
                    return SoundEffect.builder(Sound.ENTITY_WOLF_SHAKE).volume(0.6).pitch(1.7).build();
                case 27:
                    return SoundEffect.builder(Sound.BLOCK_IRON_DOOR_OPEN).volume(0.6).pitch(1.8).build();
                default:
                    return SoundEffect.NONE;
            }
        };
    }
}
