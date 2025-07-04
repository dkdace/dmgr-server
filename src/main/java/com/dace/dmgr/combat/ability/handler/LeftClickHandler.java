package com.dace.dmgr.combat.ability.handler;

import com.dace.dmgr.combat.ability.Action;
import com.dace.dmgr.combat.ability.ActionKey;

/**
 * {@link ActionKey#LEFT_CLICK}의 동작 사용 이벤트를 처리하는 인터페이스.
 *
 * @see ActionKey#LEFT_CLICK
 */
public interface LeftClickHandler {
    /**
     * 동작 사용 시 쿨타임({@link Action#getCooldown()})을 무시하는지 확인한다.
     *
     * @return 쿨타임 무시 여부
     * @implSpec {@code false}
     */
    default boolean isLeftClickIgnoreCooldown() {
        return false;
    }

    /**
     * 좌클릭을 했을 때 실행할 작업.
     */
    void onLeftClick();
}
