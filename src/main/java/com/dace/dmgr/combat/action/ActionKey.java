package com.dace.dmgr.combat.action;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 동작 사용 키 목록.
 */
@AllArgsConstructor
@Getter
public enum ActionKey {
    /** 좌클릭 */
    LEFT_CLICK("좌클릭"),
    /** 우클릭 */
    RIGHT_CLICK("우클릭"),
    /** 1번 슬롯 */
    SLOT_1("1"),
    /** 2번 슬롯 */
    SLOT_2("2"),
    /** 3번 슬롯 */
    SLOT_3("3"),
    /** 4번 슬롯 */
    SLOT_4("4"),
    /** 양손 교체(F) */
    SWAP_HAND("F"),
    /** 버리기(Q) */
    DROP("Q"),
    /** 달리기 */
    SPRINT(""),
    /** 더블 점프(SPACE) */
    SPACE("SPACE"),
    /** 웅크리기(SHIFT) */
    SNEAK("SHIFT"),
    /** 자동 사용 1번 */
    PERIODIC_1(""),
    /** 자동 사용 2번 */
    PERIODIC_2(""),
    /** 자동 사용 3번 */
    PERIODIC_3("");

    /** 키 이름 */
    private final String name;
}
