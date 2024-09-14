package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class VellionP1Info extends PassiveSkillInfo<VellionP1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 5 * 20;
    /** 이동속도 증가량 */
    public static final int SPEED = 20;
    /** 수직 이동 강도 */
    public static final double PUSH_UP = 0.2;
    /** 수평 이동 강도 */
    public static final double PUSH_SIDE = 0.6;
    /** 지속시간 (tick) */
    public static final int DURATION = 10 * 20;
    @Getter
    private static final VellionP1Info instance = new VellionP1Info();

    private VellionP1Info() {
        super(VellionP1.class, "비행",
                "",
                "§f▍ 공중에서 날아다닐 수 있습니다.",
                "§f▍ 비행 도중 §b" + TextIcon.WALK_SPEED_INCREASE + " 이동 속도§f가 빨라집니다.",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§b{0}§f {1}%", TextIcon.WALK_SPEED_INCREASE, SPEED),
                "",
                "§7§l[SPACE] §f사용",
                "",
                "§3[지속시간 종료/재사용 시]",
                "",
                "§f▍ 사용을 종료합니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                "",
                "§7§l[SPACE] §f해제");
    }
}
