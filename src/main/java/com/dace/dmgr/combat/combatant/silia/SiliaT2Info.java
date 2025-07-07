package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.TraitInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;

public final class SiliaT2Info extends TraitInfo<SiliaT2> {
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(0.3);
    /** 피해량 */
    public static final int DAMAGE = 350;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 3.7;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 0.5;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 1;

    /** 피해 점수 */
    public static final CombatScore DAMAGE_SCORE = new CombatScore("일격", 5);

    @Getter
    private static final SiliaT2Info instance = new SiliaT2Info();

    private SiliaT2Info() {
        super(SiliaT2.class, "일격",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("특수 공격으로, 칼을 휘둘러 근거리에 <:DAMAGE:광역 피해>를 입히고 <:KNOCKBACK:밀쳐냅니다>.")
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect.Function<Integer> USE = i -> PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_PLAYER_ATTACK_SWEEP).volume(1.5).pitch(1 + i * 0.1).build(),
                SoundEffect.builder(Sound.ENTITY_IRONGOLEM_ATTACK).volume(1.5).pitch(0.8 + i * 0.1).build(),
                SoundEffect.builder("random.swordhit").volume(1.5).pitch(0.7 + i * 0.1).build());
        /** 총알 궤적 - 1 */
        public static final ParticleEffect BULLET_TRAIL_1 =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, SiliaWeaponInfo.Effects.COLOR).count(8)
                        .horizontalSpread(0.15).verticalSpread(0.15).build();
        /** 총알 궤적 - 2 */
        public static final ParticleEffect BULLET_TRAIL_2 =
                ParticleEffect.Normal.builder(Particle.CRIT).count(15).horizontalSpread(0.08).verticalSpread(0.08).speed(0.08).build();
        /** 타격 */
        public static final ParticleEffect HIT =
                ParticleEffect.Normal.builder(Particle.EXPLOSION_NORMAL).count(3).horizontalSpread(0.05).verticalSpread(0.05).speed(0.05).build();
        /** 엔티티 타격 */
        public static final PlayableEffect HIT_ENTITY = PlayableEffect.list(
                SoundEffect.builder("random.stab").volume(1).pitch(0.8).pitchVariance(0.05).build(),

                ParticleEffect.Normal.builder(Particle.CRIT).count(40).speed(0.4).build());
        /** 블록 타격 */
        public static final PlayableEffect.Function<Block> HIT_BLOCK = block -> PlayableEffect.list(
                CombatEffectUtil.HIT_BLOCK_PARTICLE.apply(block, 1.5),
                CombatEffectUtil.HIT_BLOCK_SOUND.apply(block, 1.0));
    }
}
