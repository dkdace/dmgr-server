package com.dace.dmgr.combat.combatant.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class NeaceWeaponInfo extends WeaponInfo<NeaceWeapon> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.4 * 20);
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
        super(NeaceWeapon.class, RESOURCE.DEFAULT, "이중성",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("적을 공격하거나 아군을 치유할 수 있는 완드입니다.")
                        .addActionKeyInfo("마법 구체", ActionKey.LEFT_CLICK)
                        .addActionKeyInfo("치유 광선", ActionKey.RIGHT_CLICK)
                        .build(),
                        new ActionInfoLore.NamedSection("마법 구체", ActionInfoLore.Section
                                .builder("마법 구체를 발사하여 <:DAMAGE:피해>를 입힙니다.")
                                .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                                .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, COOLDOWN / 20.0)
                                .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                                .build()
                        ),
                        new ActionInfoLore.NamedSection("치유 광선", ActionInfoLore.Section
                                .builder("바라보는 아군에게 치유 광선을 고정하여 지속적으로 <:HEAL:치유>합니다. " +
                                        "<d::구원의 표식>이 있는 아군은 치유할 수 없습니다.")
                                .addValueInfo(TextIcon.HEAL, Format.PER_SECOND, HEAL.HEAL_PER_SECOND)
                                .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, HEAL.MAX_DISTANCE)
                                .build()
                        )
                )
        );
    }

    /**
     * 치유 광선의 정보.
     */
    @UtilityClass
    public static class HEAL {
        /** 초당 치유량 */
        public static final int HEAL_PER_SECOND = 250;
        /** 최대 거리 (단위: 블록) */
        public static final int MAX_DISTANCE = 15;
        /** 대상 위치 통과 불가 시 초기화 제한 시간 (tick) */
        public static final long BLOCK_RESET_DELAY = 2 * 20L;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 5;
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(0.8).pitch(1.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GHAST_SHOOT).volume(1).pitch(1.5).build()
        );
        /** 사용 (치유 광선) */
        public static final SoundEffect USE_HEAL = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GUARDIAN_ATTACK).volume(0.2).pitch(2).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.SPELL_MOB, 255, 255, 235)
                        .horizontalSpread(0.05).verticalSpread(0.05).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 255, 255, 200)
                        .count(3).horizontalSpread(0.1).verticalSpread(0.1).build()
        );
        /** 타격 */
        public static final ParticleEffect HIT = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.SPELL_MOB, 255, 255, 200)
                        .count(15).horizontalSpread(0.2).verticalSpread(0.2).build());
        /** 엔티티 타격 (치유 광선) */
        public static final ParticleEffect HIT_ENTITY_HEAL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 255, 255, 140)
                        .build());
        /** 엔티티 타격 (치유 광선 - 축복) */
        public static final ParticleEffect HIT_ENTITY_HEAL_AMPLIFY = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 140, 255, 245)
                        .build());
    }
}
