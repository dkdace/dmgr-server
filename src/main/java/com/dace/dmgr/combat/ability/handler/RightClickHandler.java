package com.dace.dmgr.combat.ability.handler;

import com.dace.dmgr.combat.ability.Action;
import com.dace.dmgr.combat.ability.ActionKey;

/**
 * {@link ActionKey#RIGHT_CLICK}의 동작 사용 이벤트를 처리하는 인터페이스.
 *
 * @see ActionKey#RIGHT_CLICK
 */
public interface RightClickHandler {
    /**
     * 동작 사용 시 쿨타임({@link Action#getCooldown()})을 무시하는지 확인한다.
     *
     * @return 쿨타임 무시 여부
     * @implSpec {@code false}
     */
    default boolean isRightClickIgnoreCooldown() {
        return false;
    }

    /**
     * 우클릭을 했을 때 실행할 작업.
     */
    void onRightClick();
}
