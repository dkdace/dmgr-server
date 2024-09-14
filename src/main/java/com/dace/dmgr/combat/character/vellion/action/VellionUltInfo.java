package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class VellionUltInfo extends UltimateSkillInfo<VellionUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 9000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = 1 * 20;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 8;
    /** 이동 속도 감소량 */
    public static final int SLOW = 50;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);
    /** 피해량 비율 */
    public static final double DAMAGE_RATIO = 0.5;
    /** 기절 시간 (tick) */
    public static final long STUN_DURATION = 1 * 20;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 15;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;
    @Getter
    private static final VellionUltInfo instance = new VellionUltInfo();

    private VellionUltInfo() {
        super(VellionUlt.class, "나락의 결계",
                "",
                "§f▍ 일정 시간동안 무적이 되어 주변 적의 §b" + TextIcon.WALK_SPEED_DECREASE + " 이동 속도",
                "§f▍ 를 느리게 하고 §5" + TextIcon.GROUNDING + " 고정§f시킵니다.",
                "§f▍ 일정 시간 후 결계가 폭발하여 탈출하지 못한 적은",
                "§f▍ §c" + TextIcon.DAMAGE + " 광역 피해§f를 입고 §5" + TextIcon.STUN + " 기절§f합니다.",
                "§f▍ 사용 중에는 움직일 수 없습니다.",
                "",
                MessageFormat.format("§f{0} {1}", TextIcon.ULTIMATE, COST),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§5{0}§f {1}m", TextIcon.RADIUS, RADIUS),
                MessageFormat.format("§b{0}§f {1}%", TextIcon.WALK_SPEED_DECREASE, SLOW),
                MessageFormat.format("§c{0}§f 적 최대 체력의 {1}%", TextIcon.DAMAGE, 100 * DAMAGE_RATIO),
                MessageFormat.format("§5{0}§f {1}초", TextIcon.STUN, STUN_DURATION / 20.0),
                "",
                "§7§l[4] §f사용");
    }
}
