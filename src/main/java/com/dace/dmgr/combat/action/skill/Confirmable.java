package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.skill.module.ConfirmModule;
import lombok.NonNull;

/**
 * 사용 전 확인이 필요한 스킬의 인터페이스.
 */
public interface Confirmable extends Skill {
    /**
     * @return 확인 모듈
     */
    @NonNull
    ConfirmModule getConfirmModule();

    /**
     * 확인 모드 활성화 시 실행할 작업.
     */
    void onCheckEnable();

    /**
     * 확인 모드 중에 매 틱마다 실행할 작업.
     *
     * @param i 인덱스
     */
    void onCheckTick(long i);

    /**
     * 확인 모드 비활성화 시 실행할 작업.
     */
    void onCheckDisable();

    /**
     * 수락 시 실행할 작업.
     */
    void onAccept();
}