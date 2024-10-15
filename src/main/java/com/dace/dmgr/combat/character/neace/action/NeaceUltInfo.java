package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class NeaceUltInfo extends UltimateSkillInfo<NeaceUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 7000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.8 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) 12 * 20;
    @Getter
    private static final NeaceUltInfo instance = new NeaceUltInfo();

    private NeaceUltInfo() {
        super(NeaceUlt.class, "치유의 성역",
                "",
                "§f▍ 체력을 최대치로 즉시 §a" + TextIcon.HEAL + " 회복§f하고 일정 시간동안",
                "§f▍ 여러 대상을 자동으로 치유합니다.",
                "§f▍ 사용 중에는 공격할 수 없습니다.",
                "",
                MessageFormat.format("§f{0} {1}", TextIcon.ULTIMATE, COST),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                "",
                "§7§l[4] §f사용");
    }
}
