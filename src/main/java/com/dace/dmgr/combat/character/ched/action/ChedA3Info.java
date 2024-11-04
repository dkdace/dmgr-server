package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class ChedA3Info extends ActiveSkillInfo<ChedA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 24 * 20L;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.6 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 60;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 30;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 7;
    /** 탐지 시간 (tick) */
    public static final long DETECT_DURATION = 6 * 20L;

    /** 탐지 점수 */
    public static final int DETECT_SCORE = 5;
    /** 처치 점수 */
    public static final int KILL_SCORE = 10;
    /** 처치 점수 제한시간 (tick) */
    public static final long KILL_SCORE_TIME_LIMIT = 7 * 20L;
    @Getter
    private static final ChedA3Info instance = new ChedA3Info();

    private ChedA3Info() {
        super(ChedA3.class, "고스트 피닉스",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("벽을 관통하는 유령 불사조를 날려보내 범위에 닿은 적을 탐지하여 아군에게 표시합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, SIZE)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DETECT_DURATION / 20.0)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build()
                )
        );
    }
}
