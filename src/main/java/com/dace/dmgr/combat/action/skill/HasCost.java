package com.dace.dmgr.combat.action.skill;

/**
 * 필요 충전량이 존재하는 스킬(주로 궁극기)의 인터페이스.
 */
public interface HasCost {
    /**
     * 스킬의 필요 충전량을 반환한다.
     *
     * @return 필요 충전량
     */
    int getCost();
}
