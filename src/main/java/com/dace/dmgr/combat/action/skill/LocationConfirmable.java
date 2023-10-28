package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.skill.module.LocationConfirmModule;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 사용 전 위치 확인이 필요한 스킬의 인터페이스.
 */
public interface LocationConfirmable extends Confirmable {
    /**
     * @return 위치 확인 모듈
     */
    LocationConfirmModule getConfirmModule();

    @Override
    @MustBeInvokedByOverriders
    default void reset() {
        getConfirmModule().reset();
    }

    @Override
    @MustBeInvokedByOverriders
    default void remove() {
        getConfirmModule().remove();
    }
}