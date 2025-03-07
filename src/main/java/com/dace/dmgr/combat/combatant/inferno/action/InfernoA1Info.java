package com.dace.dmgr.combat.combatant.inferno.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public final class InfernoA1Info extends ActiveSkillInfo<InfernoA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 5 * 20L;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.4 * 20);
    /** 수직 이동 강도 */
    public static final double PUSH_UP = 0.5;
    /** 수평 이동 강도 */
    public static final double PUSH_SIDE = 1.6;
    /** 피해량 */
    public static final int DAMAGE = 200;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 3.5;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.6;
    @Getter
    private static final InfernoA1Info instance = new InfernoA1Info();

    private InfernoA1Info() {
        super(InfernoA1.class, "점프 부스터",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("앞으로 높게 도약하여 착지할 때 <:DAMAGE:광역 피해>를 입히고 <:KNOCKBACK:밀쳐냅니다>.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build()
                )
        );
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WITHER_SHOOT).volume(3).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GHAST_SHOOT).volume(3).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(3).pitch(0.6).build()
        );
        /** 착지 */
        public static final SoundEffect LAND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(3).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(3).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(3).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(3).pitch(1.3).build()
        );
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 사용 */
        public static final ParticleEffect USE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_NORMAL).count(80).horizontalSpread(0.5).verticalSpread(0.2).speed(0.2).build());
        /** 사용 시 틱 입자 효과 */
        public static final ParticleEffect USE_TICK = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.FLAME).count(4).verticalSpread(0.15).speed(0.02).build(),
                ParticleEffect.DirectionalParticleInfo.builder(Particle.EXPLOSION_NORMAL, new Vector(0, -0.3, 0)).build()
        );
        /** 착지 (중심) */
        public static final ParticleEffect LAND_CORE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_NORMAL).count(200).horizontalSpread(0.8).verticalSpread(0.1).speed(0.05).build());
        /** 착지 (장식) - 1 */
        public static final ParticleEffect LAND_DECO_1 = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.FLAME).count(5).speed(0.05).build());
        /** 착지 (장식) - 2 */
        public static final ParticleEffect LAND_DECO_2 = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.EXPLOSION_NORMAL)
                        .speedMultiplier(0.35).build());
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(50).speed(0.4).build());
    }
}
