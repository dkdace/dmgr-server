package com.dace.dmgr.combat.ability.handler;

import com.dace.dmgr.combat.ability.ActionKey;

/**
 * {@link ActionKey#SYSTEM}의 동작 사용 이벤트를 처리하는 인터페이스.
 *
 * @see ActionKey#SYSTEM
 */
public interface SystemHandler {
    /**
     * 시스템에서 동작 사용 시 실행할 작업.
     */
    void onSystemUse();
}
