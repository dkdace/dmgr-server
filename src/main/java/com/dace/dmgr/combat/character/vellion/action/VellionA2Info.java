package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class VellionA2Info extends ActiveSkillInfo<VellionA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 5 * 20L;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.8 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 초당 피해량 */
    public static final int DAMAGE_PER_SECOND = 60;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 8;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 20;
    /** 방어력 감소량 */
    public static final int DEFENSE_DECREMENT = 25;
    /** 대상 위치 통과 불가 시 초기화 제한 시간 (tick) */
    public static final long BLOCK_RESET_DELAY = 2 * 20L;

    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;
    @Getter
    private static final VellionA2Info instance = new VellionA2Info();

    private VellionA2Info() {
        super(VellionA2.class, "저주 귀속",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("바라보는 적에게 저주를 걸어 <:DEFENSE_DECREASE:방어력>을 감소시키고 해당 적을 제외한 주변에 지속적인 <:DAMAGE:광역 피해>를 입힙니다. " +
                                "해당 적이 시야에서 " + BLOCK_RESET_DELAY / 20.0 + "초간 사라지거나 사거리를 벗어나면 저주가 풀립니다.")
                        .addValueInfo(TextIcon.DEFENSE_DECREASE, Format.PERCENT, DEFENSE_DECREMENT)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addValueInfo(TextIcon.DAMAGE, Format.PER_SECOND, DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build(),
                        new ActionInfoLore.NamedSection("취소/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                                .addActionKeyInfo("해제", ActionKey.SLOT_2)
                                .build()
                        )
                )
        );
    }
}
