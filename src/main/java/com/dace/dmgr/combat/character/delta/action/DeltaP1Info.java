package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class DeltaP1Info extends PassiveSkillInfo<DeltaP1> {
    /** 발동 시간 */
    public static final long ACTIVATE_DURATION = 2 * 20;
    /** 이동속도 증가 */
    public static final int SPEED_INCREMENT = 64;
    /** 감지 범위(m) */
    public static final int DETECT_RADIUS = 8;

    @Getter
    private static final DeltaP1Info instance = new DeltaP1Info();

    private DeltaP1Info() {
        super(DeltaP1.class, "암호화",
                "",
                "§f▍ 주변에 적이 없으면 투명해지고 §b" + TextIcon.WALK_SPEED_INCREASE + "이동 속도§f가 빨라집니다.",
                "§f▍ 기본 무기나 스킬을 사용하면 해제됩니다.",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, ACTIVATE_DURATION / 20.0),
                MessageFormat.format("§b{0}§f {1}%", TextIcon.WALK_SPEED_INCREASE, SPEED_INCREMENT),
                MessageFormat.format("§a{0}§f {1}m", TextIcon.RADIUS, DETECT_RADIUS)
        );
    }
}
