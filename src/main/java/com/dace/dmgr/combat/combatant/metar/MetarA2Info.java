package com.dace.dmgr.combat.combatant.metar;

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

public final class MetarA2Info extends ActiveSkillInfo<MetarA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(0.5);
    /** 스택 충전 쿨타임 */
    public static final Timespan STACK_COOLDOWN = Timespan.ofSeconds(7);
    /** 최대 스택 충전량 */
    public static final int MAX_STACK = 2;
    /** 체력 */
    public static final int HEALTH = 1000;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(14);

    /** 방어 점수 */
    public static final int BLOCK_SCORE = 20;
    /** 파괴 점수 */
    public static final int DEATH_SCORE = 8;

    @Getter
    private static final MetarA2Info instance = new MetarA2Info();

    private MetarA2Info() {
        super(MetarA2.class, "에너지 방벽",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("커다란 <3::에너지 방벽>을 설치하여 공격을 방어합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME_WITH_MAX_STACK, STACK_COOLDOWN.toSeconds(), MAX_STACK)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build(),
                        new ActionInfoLore.NamedSection("에너지 방벽", ActionInfoLore.Section
                                .builder("공격을 막는 고정형 방벽입니다.")
                                .addValueInfo(TextIcon.HEAL, HEALTH)
                                .build())));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDERDRAGON_FLAP).volume(1).pitch(1.3).build(),
                SoundEffect.SoundInfo.builder("random.charge").volume(1).pitch(0.7).build());
        /** 피격 */
        public static final SoundEffect DAMAGE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_POP).volume(0.4).pitch(1.5).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_IRONGOLEM_ATTACK).volume(0.4).pitch(1.4).pitchVariance(0.1).build());
        /** 파괴 */
        public static final SoundEffect DEATH = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDERMEN_TELEPORT).volume(2).pitch(2).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDERMEN_TELEPORT).volume(2).pitch(2).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDERDRAGON_HURT).volume(2).pitch(2).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 피격 */
        public static final ParticleEffect DAMAGE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT_MAGIC)
                        .count(0, 0, 1)
                        .speed(0.2).build());
        /** 파괴 */
        public static final ParticleEffect DEATH = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT_MAGIC).count(60).horizontalSpread(0.3).verticalSpread(0.3).speed(0.4).build());
    }
}
