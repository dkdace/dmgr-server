package com.dace.dmgr.combat.combatant.inferno;

import com.dace.dmgr.Timespan;
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
import org.bukkit.util.Vector;

public final class InfernoA2Info extends ActiveSkillInfo<InfernoA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(12);
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 60;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 회복량 감소 */
    public static final int HEAL_DECREMENT = 50;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(2.5);

    /** 초당 효과 점수 */
    public static final int EFFECT_SCORE_PER_SECOND = 3;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 15;

    @Getter
    private static final InfernoA2Info instance = new InfernoA2Info();

    private InfernoA2Info() {
        super(InfernoA2.class, "불꽃 방출",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 주변에 불꽃을 방출하여 <:FIRE:화염 피해>와 <:HEAL_DECREASE:회복량> 감소를 입히고 <:GROUNDING:고정>시킵니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.FIRE, Format.PER_SECOND, FIRE_DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.HEAL_DECREASE, Format.PERCENT, HEAL_DECREMENT)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_PISTON_CONTRACT).volume(2).pitch(0.5).build(),
                SoundEffect.builder(Sound.BLOCK_PISTON_CONTRACT).volume(2).pitch(0.6).build());
        /** 틱 효과음 */
        public static final PlayableEffect TICK_SOUND = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(2).pitch(0.55).pitchVariance(0.1).build(),
                SoundEffect.builder(Sound.BLOCK_FIRE_AMBIENT).volume(2).pitch(0.6).pitchVariance(0.1).build());
        /** 틱 입자 효과 - 1 */
        public static final ParticleEffect TICK_PARTICLE_1 =
                ParticleEffect.Normal.builder(Particle.FLAME).count(2).horizontalSpread(0.1).verticalSpread(0.1).speed(0.2).build();
        /** 틱 입자 효과 - 2 */
        public static final PlayableEffect.Function<Vector> TICK_PARTICLE_2 = velocity -> PlayableEffect.list(
                ParticleEffect.Directional.create(Particle.SMOKE_NORMAL, velocity.clone().multiply(0.32)),
                ParticleEffect.Directional.create(Particle.FLAME, velocity.clone().multiply(0.2)));
    }
}
