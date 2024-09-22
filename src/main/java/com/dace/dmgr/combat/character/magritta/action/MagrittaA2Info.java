package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class MagrittaA2Info extends ActiveSkillInfo<MagrittaA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 10 * 20;
    /** 이동속도 증가량 */
    public static final int SPEED = 60;
    /** 지속 시간 (tick) */
    public static final long DURATION = 1 * 20;
    @Getter
    private static final MagrittaA2Info instance = new MagrittaA2Info();

    private MagrittaA2Info() {
        super(MagrittaA2.class, "불꽃의 그림자",
                "",
                "§f▍ 짧은 시간동안 §b" + TextIcon.WALK_SPEED_INCREASE + " 이동 속도§f가 빨라지며 모든",
                "§f▍ 공격을 받지 않습니다.",
                "§f▍ 사용 후 기본 무기를 재장전합니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§b{0}§f {1}%", TextIcon.WALK_SPEED_INCREASE, SPEED),
                "",
                "§7§l[2] [우클릭] §f사용");
    }
}
