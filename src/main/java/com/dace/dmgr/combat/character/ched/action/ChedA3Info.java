package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class ChedA3Info extends ActiveSkillInfo<ChedA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 24 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.6 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 60;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 30;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 7;
    /** 탐지 시간 (tick) */
    public static final long DETECT_DURATION = 6 * 20;

    /** 탐지 점수 */
    public static final int DETECT_SCORE = 5;
    /** 처치 점수 */
    public static final int KILL_SCORE = 10;
    /** 처치 점수 제한시간 (tick) */
    public static final long KILL_SCORE_TIME_LIMIT = 7 * 20;
    @Getter
    private static final ChedA3Info instance = new ChedA3Info();

    private ChedA3Info() {
        super(ChedA3.class, "고스트 피닉스",
                "",
                "§f▍ 벽을 관통하는 유령 불사조를 날려보내 범위에",
                "§f▍ 닿은 적을 탐지하여 아군에게 표시합니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, SIZE),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DETECT_DURATION / 20.0),
                "",
                "§7§l[3] §f사용");
    }
}
