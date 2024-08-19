package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

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
        super(ArkaceUlt.class, "인피니버스터",
                "",
                "§6" + TextIcon.DURATION + " 지속시간§f동안 기본 무기에 장탄수 무한, 탄퍼짐 제거, 거리별",
                "§f피해 감소 제거 효과가 적용됩니다.",
                "",
                "§6" + TextIcon.DURATION + "§f 12초",
                "§f" + TextIcon.ULTIMATE + "§f 7000",
                "", "§7§l[4] §f사용");
    }
}
