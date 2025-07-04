package com.dace.dmgr.combat.ability;

import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * 동작 사용 키 목록.
 */
@AllArgsConstructor
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
    /** 시스템. 플레이어가 직접 사용하지 않으며, 패시브 스킬 등을 자동으로 호출할 때 사용 */
    SYSTEM("");

    /** 키 이름 */
    private final String name;

    @Override
    public String toString() {
        return name;
    }

    /**
     * 액티브 스킬에서 사용되는 슬롯 동작 사용 키 목록.
     */
    @AllArgsConstructor
    public enum Slot {
        /** 1번 슬롯 */
        SLOT_1(ActionKey.SLOT_1),
        /** 2번 슬롯 */
        SLOT_2(ActionKey.SLOT_2),
        /** 3번 슬롯 */
        SLOT_3(ActionKey.SLOT_3),
        /** 4번 슬롯 */
        SLOT_4(ActionKey.SLOT_4);

        /** 동작 사용 키 */
        private final ActionKey actionKey;

        /**
         * 해당하는 동작 사용 키를 반환한다.
         *
         * @return 동작 사용 키
         */
        @NonNull
        public ActionKey toActionKey() {
            return actionKey;
        }
    }
}
