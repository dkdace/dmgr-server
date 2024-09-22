package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class ArkaceUltInfo extends UltimateSkillInfo<ArkaceUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 7500;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) 12 * 20;

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 40;
    @Getter
    private static final ArkaceUltInfo instance = new ArkaceUltInfo();

    private ArkaceUltInfo() {
        super(ArkaceUlt.class, "오버클럭",
                "",
                "§f▍ 일정 시간동안 기본 무기의 반동과 탄퍼짐 및",
                "§f▍ 장거리 피해량 감소가 없어지고 재장전 없이",
                "§f▍ 사격할 수 있게 됩니다.",
                "",
                MessageFormat.format("§f{0} {1}", TextIcon.ULTIMATE, COST),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                "",
                "§7§l[4] §f사용");
    }
}
