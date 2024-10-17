package com.dace.dmgr.combat.entity;

import lombok.experimental.UtilityClass;

/**
 * 전투 중 상태 효과, 속성 등으로 제한되는 행동을 나타내는 비트마스크.
 * 비트 연산({@code ~}, {@code &}, {@code |}, {@code ^})을 이용해 제한될 행동의 조합을 표현할 수 있다.
 */
@UtilityClass
public final class CombatRestrictions {
    /** 행동 제한 없음 */
    public static final long NONE = 0;

    ////// 기본 마스크 //////

    /** 피해 입기 */
    public static final long DAMAGED = 1 << 0;
    /** 치료 받기 */
    public static final long HEALED = 1 << 1;
    /** 사운드 듣기 */
    public static final long HEAR = 1 << 2;

    /** 기본 이동 */
    public static final long DEFAULT_MOVE = 1 << 3;
    /** 달리기 */
    public static final long SPRINT = 1 << 4;
    /** 점프 */
    public static final long JUMP = 1 << 5;
    /** 스스로 밀음 */
    public static final long PUSH = 1 << 6;
    /** 텔레포트함 */
    public static final long TELEPORT = 1 << 7;
    /** 텔레포트함 */
    public static final long FLY = 1 << 8;


    /** 스킬 사용 */
    public static final long USE_SKILL = 1 << 9;
    /** 무기 사용 */
    public static final long USE_WEAPON = 1 << 10;
    /** 기본 근접 공격 */
    public static final long MELEE_ATTACK = 1 << 11;

    ////// 마스크 조합 //////

    /** 이동기 */
    public static final long ACTION_MOVE = SPRINT | JUMP | PUSH | TELEPORT | FLY;
    /** 행동하기 */
    public static final long USE_ACTION = USE_SKILL | USE_WEAPON | MELEE_ATTACK;
}