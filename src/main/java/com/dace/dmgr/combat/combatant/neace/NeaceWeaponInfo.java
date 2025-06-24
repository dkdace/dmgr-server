package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.WeaponInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;

public final class NeaceWeaponInfo extends WeaponInfo<NeaceWeapon> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(0.4);
    /** 피해량 */
    public static final int DAMAGE = 100;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 20;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 40;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.16;

    @Getter
    private static final NeaceWeaponInfo instance = new NeaceWeaponInfo();

    private NeaceWeaponInfo() {
        super(NeaceWeapon.class, Resource.DEFAULT, "이중성",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("적을 공격하거나 아군을 치유할 수 있는 완드입니다.")
                        .addActionKeyInfo("마법 구체", ActionKey.LEFT_CLICK)
                        .addActionKeyInfo("치유 광선", ActionKey.RIGHT_CLICK)
                        .build(),
                        new AbilityInfoLore.NamedSection("마법 구체", AbilityInfoLore.Section
                                .builder("마법 구체를 발사하여 <:DAMAGE:피해>를 입힙니다.")
                                .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                                .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, COOLDOWN.toSeconds())
                                .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                                .build()),
                        new AbilityInfoLore.NamedSection("치유 광선", AbilityInfoLore.Section
                                .builder("바라보는 아군에게 치유 광선을 고정하여 지속적으로 <:HEAL:치유>합니다.")
                                .addValueInfo(TextIcon.HEAL, Format.PER_SECOND, Heal.HEAL_PER_SECOND)
                                .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, Heal.MAX_DISTANCE)
                                .build())));
    }

    /**
     * 치유 광선의 정보.
     */
    @UtilityClass
    public static final class Heal {
        /** 초당 치유량 */
        public static final int HEAL_PER_SECOND = 250;
        /** 최대 거리 (단위: 블록) */
        public static final int MAX_DISTANCE = 15;
        /** 대상 위치 통과 불가 시 초기화 제한 시간 */
        public static final Timespan BLOCK_RESET_DELAY = Timespan.ofSeconds(2);
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static final class Resource {
        /** 기본 */
        public static final short DEFAULT = 5;
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(255, 255, 200);

        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(0.8).pitch(1.8).build(),
                SoundEffect.builder(Sound.ENTITY_GHAST_SHOOT).volume(1).pitch(1.5).build());
        /** 총알 궤적 */
        public static final PlayableEffect BULLET_TRAIL = PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, Color.fromRGB(255, 255, 235))
                        .horizontalSpread(0.05).verticalSpread(0.05).build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).count(3).horizontalSpread(0.1).verticalSpread(0.1)
                        .build());
        /** 타격 */
        public static final ParticleEffect HIT =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, COLOR).count(15).horizontalSpread(0.2)
                        .verticalSpread(0.2).build();
        /** 치유 광선 - 사용 효과음 */
        public static final SoundEffect HEAL_USE_SOUND =
                SoundEffect.builder(Sound.ENTITY_GUARDIAN_ATTACK).volume(0.2).pitch(2).build();
        /** 치유 광선 - 사용 입자 효과 */
        public static final ParticleEffect HEAL_USE_PARTICLE =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, Color.fromRGB(255, 255, 140)).build();
        /** 치유 광선 - 축복 - 사용 입자 효과 */
        public static final ParticleEffect HEAL_AMPLIFY_USE_PARTICLE =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, Color.fromRGB(140, 255, 245)).build();

        /**
         * 치유 광선 - 사용 효과를 재생한다.
         *
         * @param start        시작 위치
         * @param end          끝 위치
         * @param isAmplifying 축복 여부
         */
        public static void playHealUse(@NonNull Location start, @NonNull Location end, boolean isAmplifying) {
            for (Location loc : LocationUtil.getLine(start, end, 0.8))
                (isAmplifying ? HEAL_AMPLIFY_USE_PARTICLE : HEAL_USE_PARTICLE).play(loc);
        }
    }
}
