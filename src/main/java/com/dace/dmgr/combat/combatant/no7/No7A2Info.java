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
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class No7A2Info extends ActiveSkillInfo<No7A2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(8);
    /** 크기 (단위: 블록) */
    public static final double SIZE = 7;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(2.5);

    /** 방어 점수 */
    public static final int BLOCK_SCORE = 5;

    @Getter
    private static final No7A2Info instance = new No7A2Info();

    private No7A2Info() {
        super(No7A2.class, "능동방어 자기장",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 자기장을 둘러 주변으로 날아오는 탄환 및 투사체를 소멸시킵니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, SIZE / 2)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2, ActionKey.LEFT_CLICK)
                        .build(),
                        new ActionInfoLore.NamedSection("지속시간 종료/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("해제", ActionKey.SLOT_2, ActionKey.LEFT_CLICK)
                                .build())));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_PISTON_EXTEND).volume(1.5).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder("new.block.beacon.activate").volume(1.5).pitch(1.4).build());
        /** 해제 */
        public static final SoundEffect DISABLE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_PISTON_CONTRACT).volume(1.5).pitch(0.5).build());
        /** 틱 효과음 */
        public static final SoundEffect TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GUARDIAN_ATTACK).volume(1).pitch(1.5).build());
        /** 피격 */
        public static final SoundEffect DAMAGE = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.entity.puffer_fish.blow_out").volume(0.5).pitch(1.2).pitchVariance(0.05).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class Particles {
        /** 틱 입자 효과 */
        public static final ParticleEffect TICK = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(255, 240, 40)).build(),
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.CRIT)
                        .speedMultiplier(-0.5).build());
        /** 피격 */
        public static final ParticleEffect DAMAGE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_NORMAL).count(5).speed(0.05).build());
    }
}
