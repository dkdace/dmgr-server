package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class No7P2Info extends PassiveSkillInfo<No7P2> {
    /** 최소 초당 피해량 */
    public static final int MIN_DAMAGE_PER_SECOND = 40;
    /** 최대 초당 피해량 */
    public static final int MAX_DAMAGE_PER_SECOND = 80;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 4;

    @Getter
    private static final No7P2Info instance = new No7P2Info();

    private No7P2Info() {
        super(No7P2.class, "방전",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("<d::충전>으로 얻은 보호막을 가지고 있으면 주위에 전류를 방출하여 <:DAMAGE:광역 피해>를 입힙니다. " +
                                "보호막이 많을 수록 피해량이 증가합니다.")
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_PER_SECOND, MIN_DAMAGE_PER_SECOND, MAX_DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 엔티티 타격 */
        public static final SoundEffect HIT_ENTITY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_FIREWORK_BLAST).volume(0.4, 0.8).pitch(1.6).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class Particles {
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT)
                        .count(0, 10, 30)
                        .speed(0, 0.2, 0.6)
                        .build());
    }
}
