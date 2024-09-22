package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class JagerA1Info extends ActiveSkillInfo<JagerA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 3 * 20;
    /** 사망 시 쿨타임 (tick) */
    public static final long COOLDOWN_DEATH = 9 * 20;
    /** 소환 최대 거리 (단위: 블록) */
    public static final int SUMMON_MAX_DISTANCE = 15;
    /** 소환 시간 (tick) */
    public static final long SUMMON_DURATION = 2 * 20;
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
    public static final long KILL_SCORE_TIME_LIMIT = 10 * 20;
    /** 사망 점수 */
    public static final int DEATH_SCORE = 15;
    @Getter
    private static final JagerA1Info instance = new JagerA1Info();

    private JagerA1Info() {
        super(JagerA1.class, "설랑",
                "",
                "§f▍ 바라보는 곳에 공격을 돕는 늑대인 §3설랑§f을",
                "§f▍ 소환합니다.",
                "",
                MessageFormat.format("§f{0} {1}m", TextIcon.DISTANCE, SUMMON_MAX_DISTANCE),
                "",
                "§7§l[1] §f사용",
                "",
                "§3[설랑]",
                "",
                "§f▍ 근처의 적을 탐지하면 추적하며, §5" + TextIcon.SNARE + " 속박§f에",
                "§f▍ 걸린 적에게 §c" + TextIcon.DAMAGE_INCREASE + " 치명타§f를 입힙니다.",
                "",
                MessageFormat.format("§a{0} §f{1}", TextIcon.HEAL, HEALTH),
                MessageFormat.format("§f{0} {1}초 (사망 시)", TextIcon.COOLDOWN, COOLDOWN_DEATH / 20.0),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, ENEMY_DETECT_RADIUS),
                "",
                "§f▍ 적에게 접근하여 §c" + TextIcon.DAMAGE + " 피해§f를 입힙니다.",
                "",
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§c{0}§f 1초", TextIcon.ATTACK_SPEED),
                "",
                "§3[재사용 시]",
                "",
                "§f▍ 사용을 종료합니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                "",
                "§7§l[1] §f회수");
    }
}
