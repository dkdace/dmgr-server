package com.dace.dmgr.combat.ability.handler;

import com.dace.dmgr.combat.ability.ActionKey;

/**
 * {@link ActionKey#SPACE}의 동작 사용 이벤트를 처리하는 인터페이스.
 *
 * @see ActionKey#SPACE
 */
public interface SpaceHandler {
    /**
     * 더블 점프를 했을 때 실행할 작업.
     */
    void onSpace();
}
