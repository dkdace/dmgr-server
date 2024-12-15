package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class InfernoA2Info extends ActiveSkillInfo<InfernoA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 12 * 20L;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 60;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 회복량 감소 */
    public static final int HEAL_DECREMENT = 50;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);

    /** 초당 효과 점수 */
    public static final int EFFECT_SCORE_PER_SECOND = 3;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 15;
    @Getter
    private static final InfernoA2Info instance = new InfernoA2Info();

    private InfernoA2Info() {
        super(InfernoA2.class, "불꽃 방출",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 주변에 불꽃을 방출하여 <:FIRE:화염 피해>와 <:HEAL_DECREASE:회복량> 감소를 입히고 <:GROUNDING:고정>시킵니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.FIRE, Format.PER_SECOND, FIRE_DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.HEAL_DECREASE, Format.PERCENT, HEAL_DECREMENT)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build()
                )
        );
    }
}
