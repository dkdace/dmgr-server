package com.dace.dmgr.combat.action;

/**
 * 상호작용 종류.
 */
public enum ActionKey {
    /** 좌클릭 */
    LEFT_CLICK,
    /** 우클릭 */
    RIGHT_CLICK,
    /** 크랙샷 무기 상호작용 */
    CS_PRE_USE,
    /** 크랙샷 무기 사격 */
    CS_USE,
    /** 1번 키 */
    SLOT_1,
    /** 2번 키 */
    SLOT_2,
    /** 3번 키 */
    SLOT_3,
    /** 4번 키 */
    SLOT_4,
    /** 버리기(Q) */
    DROP,
    /** 달리기 */
    SPRINT,
    /** 웅크리기(SHIFT) */
    SNEAK;
}
