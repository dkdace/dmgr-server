package com.dace.dmgr.combat.combatant.neace;

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

public final class NeaceA3Info extends ActiveSkillInfo<NeaceA3> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(3);
    /** 이동 강도 */
    public static final double PUSH = 0.9;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 30;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(2);

    @Getter
    private static final NeaceA3Info instance = new NeaceA3Info();

    private NeaceA3Info() {
        super(NeaceA3.class, "도움의 손길",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("바라보는 아군을 향해 날아갑니다.")
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build(),
                        new ActionInfoLore.NamedSection("대상 접근/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("해제", ActionKey.SLOT_3)
                                .build())));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(1.2).pitch(1.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDERDRAGON_FLAP).volume(1.2).pitch(1.6).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_FIREWORK_LAUNCH).volume(1.2).pitch(0.7).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class Particles {
        /** 틱 입자 효과 (중심) */
        public static final ParticleEffect TICK_CORE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.FIREWORKS_SPARK).count(6).horizontalSpread(0.2).horizontalSpread(0.4).speed(0.1).build());
        /** 틱 입자 효과 (장식) */
        public static final ParticleEffect TICK_DECO = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.END_ROD).horizontalSpread(0.02).verticalSpread(0.02).build());
    }
}
