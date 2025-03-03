package com.dace.dmgr.combat.entity;

/**
 * 준비 대기시간이 있는 엔티티의 인터페이스.
 */
public interface HasReadyTime extends CombatEntity {
    /**
     * 준비 대기시간 중에 매 틱마다 실행할 작업.
     *
     * @param i 인덱스
     */
    void onTickBeforeReady(long i);

    /**
     * 준비 완료 시 실행할 작업.
     */
    void onReady();
}
