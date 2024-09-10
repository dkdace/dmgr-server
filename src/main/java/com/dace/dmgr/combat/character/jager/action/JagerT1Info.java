package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class JagerT1Info extends TraitInfo {
    /** 지속시간 (tick) */
    public static final long DURATION = 2 * 20;
    /** 달리기 불가능 수치 */
    public static final int NO_SPRINT = 60;
    /** 점프 불가능 수치 */
    public static final int NO_JUMP = 80;
    /** 최대치 */
    public static final int MAX = 100;
    @Getter
    private static final JagerT1Info instance = new JagerT1Info();

    private JagerT1Info() {
        super("빙결",
                "",
                "§f▍ 수치에 비례하여 §b" + TextIcon.WALK_SPEED_DECREASE + " 이동 속도§f가 느려지는",
                "§f▍ 상태이상입니다.",
                "§f▍ 수치가 §d60§f을 넘으면 달리기가 불가능해집니다.",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§5{0}§f 최대 {1}", TextIcon.WALK_SPEED_DECREASE, MAX),
                MessageFormat.format("§b{0}§f (빙결)%", TextIcon.WALK_SPEED_DECREASE));
    }
}
