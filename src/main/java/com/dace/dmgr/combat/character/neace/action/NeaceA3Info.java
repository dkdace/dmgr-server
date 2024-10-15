package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class NeaceA3Info extends ActiveSkillInfo<NeaceA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 3 * 20;
    /** 이동 강도 */
    public static final double PUSH = 0.9;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 30;
    /** 지속시간 (tick) */
    public static final long DURATION = 2 * 20;
    @Getter
    private static final NeaceA3Info instance = new NeaceA3Info();

    private NeaceA3Info() {
        super(NeaceA3.class, "도움의 손길",
                "",
                "§f▍ 바라보는 아군을 향해 날아갑니다.",
                "",
                MessageFormat.format("§f{0} {1}m", TextIcon.DISTANCE, MAX_DISTANCE),
                "",
                "§7§l[3] §f사용",
                "",
                "§3[대상 접근/재사용 시]",
                "",
                "§f▍ 사용을 종료합니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                "",
                "§7§l[3] §f해제");
    }
}
