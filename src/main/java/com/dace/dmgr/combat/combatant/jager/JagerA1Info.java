package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
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
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("바라보는 곳에 공격을 돕는 늑대인 <3::설랑>을 소환합니다.")
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, SUMMON_MAX_DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build(),
                        new AbilityInfoLore.NamedSection("설랑", AbilityInfoLore.Section
                                .builder("근처의 적을 탐지하면 추적합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME + " (사망 시)", COOLDOWN_DEATH.toSeconds())
                                .addValueInfo(TextIcon.HEALTH, HEALTH)
                                .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, ENEMY_DETECT_RADIUS)
                                .build()),
                        new AbilityInfoLore.NamedSection("설랑: 공격 시", AbilityInfoLore.Section
                                .builder("적에게 접근하여 <:DAMAGE:피해>를 입힙니다. " +
                                        "<:SNARE:속박>에 걸린 적에게 <:DAMAGE_INCREASE:치명타>를 입힙니다.")
                                .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                                .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, 1)
                                .build()),
                        new AbilityInfoLore.NamedSection("재사용 시", AbilityInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("회수", ActionKey.SLOT_1)
                                .build())));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 소환 준비 대기 틱 효과 */
        public static final ParticleEffect SUMMON_BEFORE_READY_TICK =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, Color.fromRGB(255, 255, 255)).count(5)
                        .horizontalSpread(0.2).verticalSpread(0.2).build();
        /** 소환 준비 */
        public static final SoundEffect SUMMON_READY =
                SoundEffect.builder(Sound.ENTITY_WOLF_GROWL).volume(1).pitch(1).build();
        /** 적 감지 */
        public static final SoundEffect ENEMY_DETECT =
                SoundEffect.builder(Sound.ENTITY_WOLF_GROWL).volume(2).pitch(0.85).build();
        /** 피격 */
        public static final PlayableEffect.TriFunction<CombatEntity, Location, Double> DAMAGE =
                (combatEntity, location, damage) -> PlayableEffect.list(
                        SoundEffect.builder(Sound.ENTITY_WOLF_HURT).volume(0.4 + damage * 0.001).pitch(1).pitchVariance(0.1).build(),
                        CombatEffectUtil.DamageParticle.BLOOD.apply(combatEntity, location, damage));
        /** 사망 */
        public static final SoundEffect DEATH =
                SoundEffect.builder(Sound.ENTITY_WOLF_DEATH).volume(1).pitch(1).pitchVariance(0.1).build();
    }
}
