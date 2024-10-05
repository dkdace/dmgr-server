package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class ChedA2Info extends ActiveSkillInfo<ChedA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 4 * 20;
    /** 수직 이동 강도 */
    public static final double PUSH_UP = 0.25;
    /** 수평 이동 강도 */
    public static final double PUSH_SIDE = 0.75;
    @Getter
    private static final ChedA2Info instance = new ChedA2Info();

    private ChedA2Info() {
        super(ChedA2.class, "윈드스텝",
                "",
                "§f▍ 이동 방향으로 짧게 도약합니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                "",
                "§7§l[2] [SPACE] §f사용");
    }
}
