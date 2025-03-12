package com.dace.dmgr.combat.combatant.jager.action;

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
import org.bukkit.Sound;

public final class JagerA1Info extends ActiveSkillInfo<JagerA1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(3);
    /** 사망 시 쿨타임 */
    public static final Timespan COOLDOWN_DEATH = Timespan.ofSeconds(9);
    /** 소환 최대 거리 (단위: 블록) */
    public static final int SUMMON_MAX_DISTANCE = 15;
    /** 소환 시간 */
    public static final Timespan SUMMON_DURATION = Timespan.ofSeconds(2);
    /** 체력 */
    public static final int HEALTH = 500;
    /** 피해량 */
    public static final int DAMAGE = 150;
    /** 이동속도 */
    public static final double SPEED = 0.45;
    /** 적 감지 범위 (단위: 블록) */
    public static final double ENEMY_DETECT_RADIUS = 20;
    /** 체력 최대 회복 시간 */
    public static final Timespan RECOVER_DURATION = Timespan.ofSeconds(6);

    /** 처치 점수 */
    public static final int KILL_SCORE = 15;
    /** 처치 점수 제한시간 */
    public static final Timespan KILL_SCORE_TIME_LIMIT = Timespan.ofSeconds(10);
    /** 사망 점수 */
    public static final int DEATH_SCORE = 15;
    @Getter
    private static final JagerA1Info instance = new JagerA1Info();

    private JagerA1Info() {
        super(JagerA1.class, "설랑",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("바라보는 곳에 공격을 돕는 늑대인 <3::설랑>을 소환합니다.")
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, SUMMON_MAX_DISTANCE)
                        .build(),
                        new ActionInfoLore.NamedSection("설랑", ActionInfoLore.Section
                                .builder("근처의 적을 탐지하면 추적합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME + " (사망 시)", COOLDOWN_DEATH.toSeconds())
                                .addValueInfo(TextIcon.HEAL, HEALTH)
                                .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, ENEMY_DETECT_RADIUS)
                                .build()
                        ),
                        new ActionInfoLore.NamedSection("설랑: 공격 시", ActionInfoLore.Section
                                .builder("적에게 접근하여 <:DAMAGE:피해>를 입힙니다. " +
                                        "<:SNARE:속박>에 걸린 적에게 <:DAMAGE_INCREASE:치명타>를 입힙니다.")
                                .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                                .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, 1)
                                .build()
                        ),
                        new ActionInfoLore.NamedSection("재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("회수", ActionKey.SLOT_1)
                                .build()
                        )
                )
        );
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 소환 준비 */
        public static final SoundEffect SUMMON_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_GROWL).volume(1).pitch(1).build());
        /** 적 감지 */
        public static final SoundEffect ENEMY_DETECT = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_GROWL).volume(2).pitch(0.85).build());
        /** 피격 */
        public static final SoundEffect DAMAGE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_HURT).volume(0.4).pitch(1).pitchVariance(0.1).build());
        /** 사망 */
        public static final SoundEffect DEATH = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_DEATH).volume(1).pitch(1).pitchVariance(0.1).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 소환 준비 대기 틱 입자 효과 */
        public static final ParticleEffect SUMMON_BEFORE_READY_TICK = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.SPELL_MOB, 255, 255, 255)
                        .count(5).horizontalSpread(0.2).verticalSpread(0.2).build());
    }
}
