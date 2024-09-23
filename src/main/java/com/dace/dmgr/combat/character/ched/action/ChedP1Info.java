package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class ChedP1Info extends PassiveSkillInfo<ChedP1> {
    /** 벽타기 이동 강도 */
    public static final double PUSH = 0.45;
    /** 벽타기 최대 횟수 */
    public static final int USE_COUNT = 10;
    /** 매달리기 최대 시간 (tick) */
    public static final long HANG_DURATION = 6 * 20;
    @Getter
    private static final ChedP1Info instance = new ChedP1Info();

    private ChedP1Info() {
        super(ChedP1.class, "궁사의 날렵함",
                "",
                "§f▍ 벽을 클릭하여 벽을 오를 수 있습니다.",
                "§f▍ 오르는 도중 §3매달리기§f를 할 수 있습니다.",
                "",
                "§7§l[좌클릭] §f사용",
                "",
                "§3[매달리기]",
                "",
                "§f▍ 벽에 매달려 위치를 고정합니다.",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, HANG_DURATION / 20.0),
                "",
                "§7§l[SHIFT] §f사용",
                "",
                "§3[매달리기 - 지속시간 종료/재사용 시]",
                "",
                "§f▍ 사용을 종료합니다.",
                "",
                "§7§l[SHIFT] §f해제");
    }
}
