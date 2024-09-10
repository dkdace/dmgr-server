package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.character.silia.Silia;
import lombok.Getter;

import java.text.MessageFormat;

public final class SiliaA3Info extends ActiveSkillInfo<SiliaA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 1 * 20;
    /** 강제 해제 쿨타임 (tick) */
    public static final long COOLDOWN_FORCE = 5 * 20;
    /** 강제 해제 피해량 비율 */
    public static final double CANCEL_DAMAGE_RATIO = 0.1;
    /** 이동속도 증가량 */
    public static final int SPEED = 20;
    /** 최대 지속시간 (tick) */
    public static final int MAX_DURATION = 10 * 20;
    /** 일격 활성화 시간 (tick) */
    public static final int ACTIVATE_DURATION = 2 * 20;
    @Getter
    private static final SiliaA3Info instance = new SiliaA3Info();

    private SiliaA3Info() {
        super(SiliaA3.class, "폭풍전야",
                "",
                "§f▍ 일정 시간동안 §b" + TextIcon.WALK_SPEED_INCREASE + " 이동 속도§f가 빨라지고 발소리",
                "§f▍ 및 모든 행동의 소음이 감소합니다.",
                "§f▍ 일정량의 피해를 입으면 해제되며, 2초동안 유지하면",
                "§f▍ 다음 기본 공격 시 §d일격§f을 날립니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN_FORCE / 20.0),
                MessageFormat.format("§7{0}§f {1}초 / {1}초 충전", TextIcon.DURATION, MAX_DURATION / 20.0),
                MessageFormat.format("§b{0}§f {1}%", TextIcon.WALK_SPEED_INCREASE, SPEED),
                MessageFormat.format("§c{0}§f {1} (강제 해제 피해량)", TextIcon.DAMAGE, Silia.getInstance().getHealth() * CANCEL_DAMAGE_RATIO),
                "",
                "§7§l[3] §f사용",
                "",
                "§3[재사용 시]",
                "",
                "§f▍ 사용을 종료합니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                "",
                "§7§l[3] §f해제");
    }
}
