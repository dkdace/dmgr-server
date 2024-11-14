package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class SiliaP1Info extends PassiveSkillInfo<SiliaP1> {
    /** 수직 이동 강도 */
    public static final double PUSH_UP = 0.55;
    /** 수평 이동 강도 */
    public static final double PUSH_SIDE = 0.35;
    @Getter
    private static final SiliaP1Info instance = new SiliaP1Info();

    private SiliaP1Info() {
        super(SiliaP1.class, "상승 기류 - 1",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("공중 점프가 가능합니다.")
                        .addActionKeyInfo("사용", ActionKey.SPACE)
                        .build()
                )
        );
    }
}
