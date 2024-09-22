package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class JagerUltInfo extends UltimateSkillInfo<JagerUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 10000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 30;
    /** 소환 시간 (tick) */
    public static final long SUMMON_DURATION = 1 * 20;
    /** 체력 */
    public static final int HEALTH = 1000;
    /** 초당 피해량 */
    public static final int DAMAGE_PER_SECOND = 100;
    /** 최소 피해 범위 (단위: 블록) */
    public static final double MIN_RADIUS = 4;
    /** 최대 피해 범위 (단위: 블록) */
    public static final double MAX_RADIUS = 12;
    /** 최대 피해 범위에 도달하는 시간 (tick) */
    public static final long MAX_RADIUS_DURATION = 5 * 20;
    /** 초당 빙결량 */
    public static final int FREEZE_PER_SECOND = 40;
    /** 지속시간 (tick) */
    public static final long DURATION = 20 * 20;

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 30;
    /** 궁극기 처치 점수 제한시간 (tick) */
    public static final long KILL_SCORE_TIME_LIMIT = 2 * 20;
    /** 파괴 점수 */
    public static final int DEATH_SCORE = 25;
    @Getter
    private static final JagerUltInfo instance = new JagerUltInfo();

    private JagerUltInfo() {
        super(JagerUlt.class, "백야의 눈폭풍",
                "",
                "§f▍ §3눈폭풍 발생기§f를 던져 긴 시간동안 §c" + TextIcon.DAMAGE + " 광역 피해",
                "§f▍ 와 §5" + TextIcon.WALK_SPEED_DECREASE + " §d빙결§f을 입히는 눈폭풍을 일으킵니다.",
                "§f▍ 눈폭풍의 범위는 시간에 따라 점차 넓어집니다.",
                "",
                MessageFormat.format("§f{0} {1}", TextIcon.ULTIMATE, COST),
                "",
                "§7§l[4] §f사용",
                "",
                "§3[눈폭풍 발생기]",
                "",
                MessageFormat.format("§a{0}§f {1}", TextIcon.HEAL, HEALTH),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§c{0}§f {1}/초", TextIcon.DAMAGE, DAMAGE_PER_SECOND),
                MessageFormat.format("§c{0}§f {1}m ~ {2}m (0초~{3}초)", TextIcon.RADIUS, MIN_RADIUS, MAX_RADIUS, MAX_RADIUS_DURATION / 20.0),
                MessageFormat.format("§5{0}§f {1}/초", TextIcon.WALK_SPEED_DECREASE, FREEZE_PER_SECOND));
    }
}
