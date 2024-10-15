package com.dace.dmgr.combat.entity.module.statuseffect;

import lombok.experimental.UtilityClass;

/**
 * 상태로 인해 제한되는 행동을 나타내는 비트마스크.
 * 비트 연산({@code ~}, {@code &}, {@code |}, {@code ^})을 이용해 제한될 행동의 조합을 표현할 수 있다.
 */
@UtilityClass
final class StatusRestrictions {
    /** 행동 제한 없음 */
    public static final long NONE = 0;

    ////// 기본 마스크 //////

    /** 피해 입기 */
    public static final long DAMAGED = 1 << 0;
    /** 피해 입기 */
    public static final long HEALED = 1 << 1;

    /** 일반 이동 */
    public static final long WALK = 1 << 2;
    /** 점프 */
    public static final long JUMP = 1 << 3;

    /** 이동기 사용 */
    public static final long MOVE_SKILL = 1 << 3;
    /** 기타 스킬 사용 */
    public static final long ETC_SKILL = 1 << 4;
    /** 무기 사용 */
    public static final long WEAPON = 1 << 5;
    /** 기타 행동 사용 */
    public static final long ETC_ACTION = 1 << 6;
    
    ////// 마스크 조합 //////

    public static final long MOVE = WALK | JUMP;

    public static final long SKILL = MOVE_SKILL | ETC_SKILL;
    public static final long ACTION = SKILL | WEAPON | ETC_ACTION;
}