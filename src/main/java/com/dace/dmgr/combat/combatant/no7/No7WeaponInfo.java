package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
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
import org.bukkit.block.Block;

public final class No7WeaponInfo extends WeaponInfo<No7Weapon> {
    /** 연사속도 */
    public static final FullAuto.FireRate FIRE_RATE = FullAuto.FireRate.RPM_300;
    /** 피해량 */
    public static final int DAMAGE = 16;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 20;
    /** 거리별 피해량 */
    public static final DistantDamage DISTANT_DAMAGE = new DistantDamage(DAMAGE, DISTANCE);
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
                new AbilityInfoLore(AbilityInfoLore.Section
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
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final SoundEffect USE =
                SoundEffect.builder("random.energy").volume(2.5).pitch(2).build();
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, Color.fromRGB(255, 40, 40)).build();
        /** 블록 타격 효과음 */
        public static final PlayableEffect.Function<Block> HIT_BLOCK_SOUND = block -> PlayableEffect.list(
                CombatEffectUtil.BULLET_HIT_BLOCK_SOUND,
                CombatEffectUtil.HIT_BLOCK_SOUND.apply(block, 1.0));
        /** 블록 타격 입자 효과 */
        public static final PlayableEffect.Function<Block> HIT_BLOCK_PARTICLE = block ->
                CombatEffectUtil.HIT_BLOCK_SMALL_PARTICLE.apply(block, 1.0);
    }
}
