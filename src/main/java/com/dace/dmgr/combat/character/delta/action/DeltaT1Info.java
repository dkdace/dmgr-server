package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

public final class DeltaT1Info extends TraitInfo {
    /** 넉백 저항력 (%) */
    public static final int KNOCKBACK_RESISTANCE = 100;

    @Getter
    private static final DeltaT1Info instance = new DeltaT1Info();

    private DeltaT1Info() {
        super("잠금",
                "",
                "§f▍ 일정 시간동안 아무것도 할 수 없고, 쿨타임이 흐르지 않으며 모든 피해와 치유를",
                "§f▍ 받지 못하는 상태이상입니다. 또한 자신의 위치가 적에게 드러납니다."
        );
    }
}
