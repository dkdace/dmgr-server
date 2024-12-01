package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class JagerA1Info extends ActiveSkillInfo<JagerA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 3 * 20L;
    /** 사망 시 쿨타임 (tick) */
    public static final long COOLDOWN_DEATH = 9 * 20L;
    /** 소환 최대 거리 (단위: 블록) */
    public static final int SUMMON_MAX_DISTANCE = 15;
    /** 소환 시간 (tick) */
    public static final long SUMMON_DURATION = 2 * 20L;
    /** 체력 */
    public static final int HEALTH = 500;
    /** 피해량 */
    public static final int DAMAGE = 150;
    /** 이동속도 */
    public static final double SPEED = 0.45;
    /** 적 감지 범위 (단위: 블록) */
    public static final double ENEMY_DETECT_RADIUS = 20;
    /** 체력 최대 회복 시간 (tick) */
    public static final int RECOVER_DURATION = 6 * 20;

    /** 처치 점수 */
    public static final int KILL_SCORE = 15;
    /** 처치 점수 제한시간 (tick) */
    public static final long KILL_SCORE_TIME_LIMIT = 10 * 20L;
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
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME + " (사망 시)", COOLDOWN_DEATH / 20.0)
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
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
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
        public static final DefinedSound SUMMON_READY = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_GROWL, 1, 1));
        /** 적 감지 */
        public static final DefinedSound ENEMY_DETECT = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_GROWL, 2, 0.85));
        /** 피격 */
        public static final DefinedSound DAMAGE = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_HURT, 0.4, 1, 0.1));
        /** 사망 */
        public static final DefinedSound DEATH = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_DEATH, 1, 1, 0.1));
    }
}
