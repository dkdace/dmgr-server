package com.dace.dmgr.combat.ability.handler;

import com.dace.dmgr.combat.ability.ActionKey;

/**
 * {@link ActionKey#SWAP_HAND}의 동작 사용 이벤트를 처리하는 인터페이스.
 *
 * @see ActionKey#SWAP_HAND
 */
public interface SwapHandHandler {
    /**
     * 양손 교체를 했을 때 실행할 작업.
     */
    void onSwapHand();
}
