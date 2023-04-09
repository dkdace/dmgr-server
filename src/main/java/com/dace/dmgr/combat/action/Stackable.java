package com.dace.dmgr.combat.action;

/**
 * 여러 번 사용할 수 있는 스택형 스킬의 인터페이스.
 */
public interface Stackable {
    /**
     * 최대 스택 충전량을 반환한다.
     *
     * @return 최대 스택 충전량
     */
    int getMaxStack();

    /**
     * 스킬의 스택 충전 쿨타임을 반환한다.
     *
     * @return 스택 충전 쿨타임
     */
    long getStackCooldown();
}
