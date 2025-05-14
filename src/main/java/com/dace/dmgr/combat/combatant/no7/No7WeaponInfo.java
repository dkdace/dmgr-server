package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;

public final class No7WeaponInfo extends WeaponInfo<No7Weapon> {
    /** 연사속도 */
    public static final FullAuto.FireRate FIRE_RATE = FullAuto.FireRate.RPM_300;
    /** 피해량 */
    public static final int DAMAGE = 17;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 20;
    /** 산탄 수 */
    public static final int PELLET_AMOUNT = 5;
    /** 탄퍼짐 */
    public static final double SPREAD = 13;
    /** 사용 시 이동속도 감소 시간 */
    public static final Timespan SLOW_DURATION = Timespan.ofSeconds(0.3);
    /** 사용 시 이동속도 감소량 */
    public static final int SLOW = 20;

    @Getter
    private static final No7WeaponInfo instance = new No7WeaponInfo();

    private No7WeaponInfo() {
        super(No7Weapon.class, Resource.DEFAULT, "EB-M340",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("재장전이 필요 없는 에너지 산탄총입니다. 사격하여 <:DAMAGE:피해>를 입힙니다.")
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_WITH_DISTANCE + " (×{4})",
                                DAMAGE, DAMAGE / 2, DISTANCE / 2, DISTANCE, PELLET_AMOUNT)
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME_WITH_RPM,
                                60.0 / FIRE_RATE.getRoundsPerMinute(), FIRE_RATE.getRoundsPerMinute())
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addActionKeyInfo("사격", ActionKey.RIGHT_CLICK)
                        .build()));
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static final class Resource {
        /** 기본 */
        public static final short DEFAULT = 16;
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.energy").volume(2.5).pitch(2).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class Particles {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(255, 40, 40)).build());
    }
}
