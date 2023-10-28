package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.skill.module.ConfirmModule;

/**
 * 사용 전 확인이 필요한 스킬의 인터페이스.
 *
 * @see LocationConfirmable
 */
public interface Confirmable extends Skill {
    /**
     * @return 확인 모듈
     */
    ConfirmModule getConfirmModule();

    /**
     * 확인 모드 활성화 시 실행할 작업.
     */
    default void onCheckEnable() {
    }

    /**
     * 확인 모드 중에 매 tick마다 실행할 작업.
     *
     * @param i 인덱스
     */
    default void onCheckTick(int i) {
    }

    /**
     * 확인 모드 비활성화 시 실행할 작업.
     */
    default void onCheckDisable() {
    }

    /**
     * 수락 시 실행할 작업.
     */
    void onAccept();
}