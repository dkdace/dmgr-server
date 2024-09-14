package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class SiliaUltInfo extends UltimateSkillInfo<SiliaUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 8000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = 1 * 20;
    /** 지속시간 (tick) */
    public static final long DURATION = 4 * 20;
    /** 처치 시 지속시간 증가 (tick) */
    public static final long DURATION_ADD_ON_KILL = 2 * 20;
    /** 이동속도 증가량 */
    public static final int SPEED = 30;
    /** 일격 쿨타임 (tick) */
    public static final long STRIKE_COOLDOWN = (long) (0.55 * 20);

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 25;
    @Getter
    private static final SiliaUltInfo instance = new SiliaUltInfo();

    private SiliaUltInfo() {
        super(SiliaUlt.class, "폭풍의 부름",
                "",
                "§f▍ 일정 시간동안 §b" + TextIcon.WALK_SPEED_INCREASE + " 이동 속도§f가 빨라지고 기본",
                "§f▍ 공격 시 §d일격§f을 날립니다.",
                "§f▍ 적 처치 시 §7" + TextIcon.DURATION + " 지속 시간§f이 늘어나며, 사용 중에는",
                "§f▍ §d진권풍§f, §d폭풍전야§f를 사용할 수 없습니다.",
                "",
                MessageFormat.format("§f{0} {1}", TextIcon.ULTIMATE, COST),
                MessageFormat.format("§7{0}§f {1}초 (+{2}초)", TextIcon.DURATION, DURATION / 20.0, DURATION_ADD_ON_KILL / 20.0),
                MessageFormat.format("§b{0}§f {1}%", TextIcon.WALK_SPEED_INCREASE, SPEED),
                "",
                "§7§l[4] §f사용");
    }
}
