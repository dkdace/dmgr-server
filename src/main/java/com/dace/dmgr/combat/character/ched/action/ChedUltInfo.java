package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class ChedUltInfo extends UltimateSkillInfo<ChedUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 10000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (1.5 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 피해량 */
    public static final int DAMAGE = 1500;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 7;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 1;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 200;
    /** 화염 지대 지속 시간 (tick) */
    public static final long FIRE_FLOOR_DURATION = 8 * 20;
    /** 화염 지대 범위 (단위: 블록) */
    public static final double FIRE_FLOOR_RADIUS = 7;

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 20;
    /** 궁극기 처치 점수 제한시간 (tick) */
    public static final long KILL_SCORE_TIME_LIMIT = 2 * 20;
    @Getter
    private static final ChedUltInfo instance = new ChedUltInfo();

    private ChedUltInfo() {
        super(ChedUlt.class, "피닉스 스트라이크",
                "",
                "§f▍ 벽을 관통하는 불사조를 날려보내 적과",
                "§f▍ 부딪히면 크게 폭발하여 §c" + TextIcon.DAMAGE + " 광역 피해§f를 입히고",
                "§f▍ §3화염 지대§f를 만듭니다.",
                "§f▍ 플레이어가 아닌 적은 통과합니다.",
                "",
                MessageFormat.format("§f{0} {1}", TextIcon.ULTIMATE, COST),
                MessageFormat.format("§c{0}§f {1} ~ {2}", TextIcon.DAMAGE, DAMAGE, DAMAGE / 2),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, SIZE),
                "",
                "§7§l[4] §f사용",
                "",
                "§3[화염 지대]",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, FIRE_FLOOR_DURATION / 20.0),
                MessageFormat.format("§c{0}§f {1}/초", TextIcon.FIRE, FIRE_DAMAGE_PER_SECOND),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, FIRE_FLOOR_RADIUS));
    }
}
