package com.dace.dmgr.combat.combatant.inferno;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class InfernoA1Info extends ActiveSkillInfo<InfernoA1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(5);
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(0.4);
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
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_WITHER_SHOOT).volume(3).pitch(0.8).build(),
                SoundEffect.builder(Sound.ENTITY_GHAST_SHOOT).volume(3).pitch(0.8).build(),
                SoundEffect.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(3).pitch(0.6).build(),

                ParticleEffect.Normal.builder(Particle.EXPLOSION_NORMAL).count(80).horizontalSpread(0.5).verticalSpread(0.2).speed(0.2).build());
        /** 사용 시 틱 효과 */
        public static final PlayableEffect USE_TICK = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.FLAME).count(4).verticalSpread(0.15).speed(0.02).build(),
                ParticleEffect.Directional.create(Particle.EXPLOSION_NORMAL, new Vector(0, -0.3, 0)));
        /** 착지 - 1 */
        public static final PlayableEffect.Function<Block> LAND_1 = block -> PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(3).pitch(0.5).build(),
                SoundEffect.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(3).pitch(0.7).build(),
                SoundEffect.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(3).pitch(0.5).build(),
                SoundEffect.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(3).pitch(1.3).build(),

                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).count(200).horizontalSpread(0.8).verticalSpread(0.1).speed(0.05).build(),
                CombatEffectUtil.HIT_BLOCK_PARTICLE.apply(block, 5.0));
        /** 착지 - 2 */
        public static final ParticleEffect LAND_2 =
                ParticleEffect.Normal.builder(Particle.FLAME).count(5).speed(0.05).build();
        /** 착지 - 3 */
        public static final PlayableEffect.Function<Vector> LAND_3 = velocity ->
                ParticleEffect.Directional.create(Particle.EXPLOSION_NORMAL, velocity.clone().multiply(0.35));
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY =
                ParticleEffect.Normal.builder(Particle.CRIT).count(50).speed(0.4).build();
    }
}
