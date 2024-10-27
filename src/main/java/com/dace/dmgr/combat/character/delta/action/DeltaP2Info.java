package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public class DeltaP2Info extends PassiveSkillInfo<DeltaP2> {
    /** 주시 시간 */
    public static final int GAZING_DURATION = 2 * 20;
    /** 탐지 범위 (단위: 블록) */
    public static final int DETECT_RADIUS = 64;

    @Getter
    private static final DeltaP2Info instance = new DeltaP2Info();

    private DeltaP2Info() {
        super(DeltaP2.class, "뉴럴링크",
                "§f▍ 델타의 시야에 일정 시간 이상 포착된 적을 아군에게 표시합니다.",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, GAZING_DURATION / 20.0),
                MessageFormat.format("§a{0}§f {1}m", TextIcon.RADIUS, DETECT_RADIUS)
        );
    }
}
