package com.dace.dmgr.combat.action.weapon;

/**
 * 정조준이 가능한 무기의 인터페이스.
 */
public interface Aimable {
    /**
     * @return 정조준 상태
     */
    boolean isAiming();

    /**
     * 무기의 조준경 타입을 반환한다.
     *
     * @return 조준경 타입
     */
    int getScope();

    /**
     * 정조준 이벤트를 호출한다.
     */
    void aim();
}