package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

public final class QuakerUltInfo extends UltimateSkillInfo<QuakerUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 6500;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.8 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.5 * 20);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 20;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 35;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 1.5;
    /** 기절 시간 (tick) */
    public static final long STUN_DURATION = 20;
    /** 이동 속도 감소량 */
    public static final int SLOW = 30;
    /** 이동 속도 감소 시간 (tick) */
    public static final long SLOW_DURATION = 12 * 20L;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 3;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 15;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 30;
    @Getter
    private static final QuakerUltInfo instance = new QuakerUltInfo();

    private QuakerUltInfo() {
        super(QuakerUlt.class, "심판의 문지기",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("방패로 충격파를 일으켜 <:DAMAGE:광역 피해>와 <:STUN:기절>을 입히고 크게 <:KNOCKBACK:밀쳐냅니다>. " +
                                "맞은 적은 긴 시간동안 <:WALK_SPEED_DECREASE:이동 속도>가 느려집니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.STUN, Format.TIME, STUN_DURATION / 20.0)
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, Format.TIME_WITH_PERCENT, SLOW_DURATION / 20.0, SLOW)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()
                )
        );
    }
}
