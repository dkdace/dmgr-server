package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class SiliaP2Info extends PassiveSkillInfo<SiliaP2> {
    /** 벽타기 이동 강도 */
    public static final double PUSH = 0.45;
    /** 벽타기 최대 횟수 */
    public static final int USE_COUNT = 10;
    @Getter
    private static final SiliaP2Info instance = new SiliaP2Info();

    private SiliaP2Info() {
        super(SiliaP2.class, "상승 기류 - 2",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("벽을 클릭하여 벽을 오를 수 있습니다.")
                        .addActionKeyInfo("사용", ActionKey.LEFT_CLICK)
                        .build()
                )
        );
    }
}
