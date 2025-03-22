package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.Timespan;
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

public final class QuakerA3Info extends ActiveSkillInfo<QuakerA3> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(8);
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(0.8);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.3);
    /** 피해량 */
    public static final int DAMAGE = 150;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 40;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 1.2;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 2;
    /** 속박 시간 */
    public static final Timespan SNARE_DURATION = Timespan.ofSeconds(0.5);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 2;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 4;

    @Getter
    private static final QuakerA3Info instance = new QuakerA3Info();

    private QuakerA3Info() {
        super(QuakerA3.class, "돌풍 강타",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("검기를 날려 처음 맞은 적을 크게 <:KNOCKBACK:밀쳐내고> <:DAMAGE:피해>와 <:SNARE:속박>을 입힙니다. " +
                                "적이 벽에 충돌하면 같은 효과를 다시 입히며, 날아가며 부딪힌 적에게도 같은 효과를 입힙니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.SNARE, Format.TIME, SNARE_DURATION.toSeconds())
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(1).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder("random.gun2.shovel_leftclick").volume(1).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder("random.gun2.shovel_leftclick").volume(1).pitch(0.8).build());
        /** 사용 준비 */
        public static final SoundEffect USE_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GHAST_SHOOT).volume(2).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder("new.item.trident.throw").volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_SWEEP).volume(2).pitch(0.7).build());
        /** 틱 효과음 */
        public static final SoundEffect TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GHAST_SHOOT).volume(0.6).pitch(0.5).build());
        /** 타격 */
        public static final SoundEffect HIT = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(2).pitch(0.6).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_CRIT).volume(2).pitch(0.7).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 (이펙트 - 중심) */
        public static final ParticleEffect BULLET_TRAIL_EFFECT_CORE = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, QuakerWeaponInfo.PARTICLE.COLOR)
                        .count(2).horizontalSpread(0.12).verticalSpread(0.12).build());
        /** 총알 궤적 (이펙트 - 장식) */
        public static final ParticleEffect BULLET_TRAIL_EFFECT_DECO = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(3).horizontalSpread(0.07).verticalSpread(0.07).build());
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.EXPLOSION_NORMAL)
                        .speedMultiplier(1.4).build());
        /** 타격 */
        public static final ParticleEffect HIT = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_NORMAL).count(50).horizontalSpread(0.2).verticalSpread(0.2).speed(0.4)
                        .build());
        /** 엔티티 타격 (중심) */
        public static final ParticleEffect HIT_ENTITY_CORE = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.EXPLOSION_NORMAL)
                        .speedMultiplier(0.6).build());
        /** 엔티티 타격 (장식) */
        public static final ParticleEffect HIT_ENTITY_DECO = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(50).speed(0.4).build());
    }
}
