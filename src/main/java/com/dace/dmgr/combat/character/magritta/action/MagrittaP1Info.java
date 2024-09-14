package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class MagrittaP1Info extends PassiveSkillInfo<MagrittaP1> {
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 80;
    /** 감지 범위 (단위: 블록) */
    public static final double DETECT_RADIUS = 15;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (1.5 * 20);
    @Getter
    private static final MagrittaP1Info instance = new MagrittaP1Info();

    private MagrittaP1Info() {
        super(MagrittaP1.class, "방화광",
                "",
                "§f▍ 근처에 불타는 적이 존재하면 §a" + TextIcon.HEAL + " 회복§f합니다.",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§a{0}§f {1}/초", TextIcon.HEAL, HEAL_PER_SECOND),
                MessageFormat.format("§a{0}§f {1}m", TextIcon.RADIUS, DETECT_RADIUS));
    }
}
