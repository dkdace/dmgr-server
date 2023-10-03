package com.dace.dmgr.combat.action.skill;

/**
 * 사용 전 확인이 필요한 스킬의 인터페이스.
 */
public interface Confirmable {
    /**
     * @return 확인 중 여부
     */
    boolean isConfirming();

    /**
     * 확인 완료 이벤트를 호출한다.
     */
    void confirm();
}