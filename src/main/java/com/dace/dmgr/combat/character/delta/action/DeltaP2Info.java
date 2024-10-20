package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public class DeltaP2Info extends PassiveSkillInfo<DeltaP2> {
    /** 탐지 범위 (단위: 블록) */
    public static final int DETECT_RADIUS = 32;

    @Getter
    private static final DeltaP2Info instance = new DeltaP2Info();

    private DeltaP2Info() {
        super(DeltaP2.class, "취약점 탐지",
                "§f▍ 체력이 절반 이하인 적의 위치를 탐지합니다.",
                "",
                MessageFormat.format("§a{0}§f {1}m", TextIcon.RADIUS, DETECT_RADIUS)
        );
    }
}
