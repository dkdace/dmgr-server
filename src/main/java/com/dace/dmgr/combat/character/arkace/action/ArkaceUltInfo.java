package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

public final class ArkaceUltInfo extends UltimateSkillInfo<ArkaceUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 7500;
    /** 지속시간 (tick) */
    public static final long DURATION = 12 * 20L;

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 40;
    @Getter
    private static final ArkaceUltInfo instance = new ArkaceUltInfo();

    private ArkaceUltInfo() {
        super(ArkaceUlt.class, "오버클럭",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 기본 무기의 반동과 탄퍼짐 및 장거리 피해량 감소가 없어지고 재장전 없이 사격할 수 있게 됩니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()
                )
        );
    }
}
