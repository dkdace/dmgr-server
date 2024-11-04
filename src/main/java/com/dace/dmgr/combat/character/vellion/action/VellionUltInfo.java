package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

public final class VellionUltInfo extends UltimateSkillInfo<VellionUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 9000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = 20;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 8;
    /** 이동 속도 감소량 */
    public static final int SLOW = 50;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);
    /** 피해량 비율 */
    public static final double DAMAGE_RATIO = 0.5;
    /** 기절 시간 (tick) */
    public static final long STUN_DURATION = 20;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 15;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;
    @Getter
    private static final VellionUltInfo instance = new VellionUltInfo();

    private VellionUltInfo() {
        super(VellionUlt.class, "나락의 결계",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 무적이 되어 주변 적의 <:WALK_SPEED_DECREASE:이동 속도>를 느리게 하고 <:GROUNDING:고정>시킵니다. " +
                                "일정 시간 후 결계가 폭발하여 탈출하지 못한 적은 <:DAMAGE:광역 피해>를 입고 <:STUN:기절>합니다. " +
                                "사용 중에는 움직일 수 없습니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, Format.PERCENT, SLOW)
                        .addValueInfo(TextIcon.DAMAGE, "적 최대 체력의 {0}%", (int) (100 * DAMAGE_RATIO))
                        .addValueInfo(TextIcon.STUN, Format.TIME, STUN_DURATION / 20.0)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()
                )
        );
    }
}
