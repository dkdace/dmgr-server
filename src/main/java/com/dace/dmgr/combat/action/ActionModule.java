package com.dace.dmgr.combat.action;

/**
 * 동작 모듈의 인터페이스.
 */
public interface ActionModule {
    /**
     * {@link Action#reset()} 호출 시 실행할 작업.
     */
    default void onReset() {
    }

    /**
     * {@link Action#remove()} 호출 시 실행할 작업.
     */
    default void onRemove() {
    }
}
