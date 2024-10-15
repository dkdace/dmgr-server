package com.dace.dmgr.combat.entity.module.statuseffect;

import lombok.experimental.UtilityClass;

/**
 * 상태로 인해 제한되는 행동을 나타내는 비트마스크.
 * 비트 연산({@code ~}, {@code &}, {@code |}, {@code ^})을 이용해 제한될 행동의 조합을 표현할 수 있다.
 */
@UtilityClass
public final class StatusRestrictions {
    /** 행동 제한 없음 */
    public static final long NONE = 0;

    ////// 기본 마스크 //////

    /** 피해 입기 */
    public static final long DAMAGED = 1 << 0;
    /** 치료 받기 */
    public static final long HEALED = 1 << 1;
    /** 사운드 듣기 */
    public static final long HEAR = 1 << 2;

    /** 일반 이동 */
    public static final long WALK = 1 << 3;
    /** 점프 */
    public static final long JUMP = 1 << 4;

    /** (스킬 등으로) 밀려남 */
    public static final long PUSHED = 1 << 5;
    /** (스킬 등으로) 텔레포트됨 */
    public static final long TELEPORTED = 1 << 6;
    /** 스킬 사용 */
    public static final long USE_SKILL = 1 << 7;
    /** 무기 사용 */
    public static final long USE_WEAPON = 1 << 8;
    
    ////// 마스크 조합 //////

    /** 이동하기 (자발적으로) */
    public static final long MOVE = WALK | JUMP;
    /** 행동하기 */
    public static final long DO_ACTION = USE_SKILL | USE_WEAPON;
    /** 이동됨 (스킬 등에 의해) */
    public static final long MOVED = PUSHED | TELEPORTED;
}