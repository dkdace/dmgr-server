package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class InfernoP1Info extends PassiveSkillInfo<InfernoP1> {
    /** 방어력 증가량 */
    public static final int DEFENSE_INCREMENT = 30;
    /** 감지 범위 (단위: 블록) */
    public static final double DETECT_RADIUS = 10;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (1.5 * 20);
    @Getter
    private static final InfernoP1Info instance = new InfernoP1Info();

    private InfernoP1Info() {
        super(InfernoP1.class, "불꽃의 용기",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("근처에 <:FIRE:불>타는 적이 존재하면 <:DEFENSE_INCREASE:방어력>이 증가합니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.DEFENSE_INCREASE, Format.PERCENT, DEFENSE_INCREMENT)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, DETECT_RADIUS)
                        .build()
                )
        );
    }
}
