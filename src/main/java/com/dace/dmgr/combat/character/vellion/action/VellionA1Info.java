package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class VellionA1Info extends ActiveSkillInfo<VellionA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 6 * 20L;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (1.7 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.5 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 초당 독 피해량 */
    public static final int POISON_DAMAGE_PER_SECOND = 120;
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 160;
    /** 효과 지속 시간 (tick) */
    public static final long EFFECT_DURATION = (long) (2.5 * 20);
    /** 속박 시간 (tick) */
    public static final long SNARE_DURATION = (long) (0.1 * 20);
    /** 회수 시간 (tick) */
    public static final long RETURN_DURATION = (long) (0.75 * 20);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 2.5;

    /** 효과 점수 */
    public static final int EFFECT_SCORE = 1;
    @Getter
    private static final VellionA1Info instance = new VellionA1Info();

    private VellionA1Info() {
        super(VellionA1.class, "마력 집중",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("개체를 관통하는 마력 응집체를 날려 적에게는 <:POISON:독 피해>와 짧은 <:SNARE:속박>을 입히고, 아군에게는 지속적인 <:HEAL:치유> 효과를 줍니다. " +
                                "벽이나 최대 사거리에 도달하면 되돌아오며 효과를 다시 입힙니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.POISON, Format.TIME_WITH_PER_SECOND, EFFECT_DURATION / 20.0, POISON_DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.SNARE, Format.TIME, SNARE_DURATION / 20.0)
                        .addValueInfo(TextIcon.HEAL, Format.TIME_WITH_PER_SECOND, EFFECT_DURATION / 20.0, HEAL_PER_SECOND)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, VELOCITY * RETURN_DURATION / 20)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1, ActionKey.RIGHT_CLICK)
                        .build()
                )
        );
    }
}
