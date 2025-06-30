package com.dace.dmgr.combat.ability.skill;

import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.module.ConfirmModule;
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
     * 확인 수락에 사용할 수락 키를 반환한다.
     *
     * @return 수락 키
     */
    @NonNull
    ActionKey getAcceptKey();

    /**
     * 확인 취소에 사용할 취소 키를 반환한다.
     *
     * @return 취소 키
     */
    @NonNull
    ActionKey getCancelKey();

    /**
     * 확인 수락 시 실행할 작업.
     */
    void onAccept();
}