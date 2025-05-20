package com.dace.dmgr.combat.combatant.no7;

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
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class No7A3Info extends ActiveSkillInfo<No7A3> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(9);
    /** 보호막 */
    public static final int SHIELD = 300;
    /** 감지 범위 (단위: 블록) */
    public static final double DETECT_RADIUS = 8;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(1);

    /** 회복 점수 */
    public static final int SHIELD_SCORE = 4;

    @Getter
    private static final No7A3Info instance = new No7A3Info();

    private No7A3Info() {
        super(No7A3.class, "적응형 보호막",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 <e:HEAL:보호막>을 얻습니다. 근처에 적이 많을 수록 획득량이 증가합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.HEAL, "{0}+(적 수)×{1}", ChatColor.YELLOW, SHIELD, SHIELD)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, DETECT_RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(1.5).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_PISTON_CONTRACT).volume(1.5).pitch(1.6).build());
        /** 틱 효과음 */
        public static final SoundEffect TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.charge").volume(0.6).pitch(1.2).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class Particles {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(255, 255, 43);

        /** 틱 입자 효과 (중심) */
        public static final ParticleEffect TICK_CORE = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, COLOR).count(4)
                        .verticalSpread(0.8).build());
        /** 틱 입자 효과 (장식) */
        public static final ParticleEffect TICK_DECO = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.CRIT)
                        .speedMultiplier(0.5).build());
    }
}
