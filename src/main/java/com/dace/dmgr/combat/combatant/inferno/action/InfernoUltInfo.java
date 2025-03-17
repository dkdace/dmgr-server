package com.dace.dmgr.combat.combatant.inferno.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class InfernoUltInfo extends UltimateSkillInfo<InfernoUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 8000;
    /** 보호막 */
    public static final int SHIELD = 2500;
    /** 액티브 1번 쿨타임 단축 */
    public static final Timespan A1_COOLDOWN_DECREMENT = Timespan.ofSeconds(3);
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(10);

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 20;

    @Getter
    private static final InfernoUltInfo instance = new InfernoUltInfo();

    private InfernoUltInfo() {
        super(InfernoUlt.class, "과부하",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 몸에 화염 방벽을 둘러 <e:HEAL:보호막>을 얻고 <d::점프 부스터>의 <7:COOLDOWN:쿨타임>을 단축하며, 재장전 없이 사격할 수 있게 됩니다. " +
                                "보호막이 파괴되면 사용이 종료됩니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.HEAL, ChatColor.YELLOW, SHIELD)
                        .addValueInfo(TextIcon.COOLDOWN_DECREASE, Format.TIME, -A1_COOLDOWN_DECREMENT.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.block.respawn_anchor.ambient").volume(3).pitch(1.2, 1.7).build());
        /** 틱 효과음 */
        public static final SoundEffect TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_AMBIENT).volume(2).pitch(0.9).pitchVariance(0.1).build());
        /** 피격 */
        public static final SoundEffect DAMAGE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_POP).volume(0.3).pitch(1.2).pitchVariance(0.1).build());
        /** 파괴 */
        public static final SoundEffect DEATH = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(3).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(3).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder("new.block.conduit.deactivate").volume(3).pitch(0.8).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 사용 시 틱 입자 효과 (중심) */
        public static final ParticleEffect USE_TICK_CORE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.LAVA).count(3).horizontalSpread(1).verticalSpread(1.5).speed(0.2).build());
        /** 사용 시 틱 입자 효과 (장식) */
        public static final ParticleEffect USE_TICK_DECO = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.FLAME)
                        .speedMultiplier(0.2).build(),
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.SMOKE_NORMAL)
                        .speedMultiplier(0.2).build());
        /** 틱 입자 효과 (중심) */
        public static final ParticleEffect TICK_CORE = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 255, 70, 0)
                        .count(3).verticalSpread(1).build());
        /** 틱 입자 효과 (장식) */
        public static final ParticleEffect TICK_DECO = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.SMOKE_NORMAL)
                        .speedMultiplier(0.15).build());
        /** 피격 */
        public static final ParticleEffect DAMAGE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.FIRE, 0)
                        .count(0, 0, 1)
                        .speed(0.1).build());
        /** 파괴 */
        public static final ParticleEffect DEATH = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.FLAME).count(300).horizontalSpread(0.4).verticalSpread(0.4).speed(0.2).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_NORMAL).count(250).horizontalSpread(0.3).verticalSpread(0.3).speed(0.25).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_LARGE).count(150).horizontalSpread(0.4).verticalSpread(0.4).speed(0.2).build());
    }
}
