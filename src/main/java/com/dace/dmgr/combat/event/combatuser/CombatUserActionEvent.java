package com.dace.dmgr.combat.event.combatuser;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.CombatUserEvent;
import lombok.Getter;

/**
 * 플레이어가 동작 사용 키를 입력할 시 호출되는 이벤트.
 *
 * <p>동작 사용 키의 종류는 {@link ActionKey}에 있다.</p>
 *
 * @see ActionKey
 */
@Getter
public class CombatUserActionEvent extends CombatUserEvent {
    /** 동작 사용 키 종류 */
    private final ActionKey actionKey;

    /**
     * 이벤트를 생성한다.
     *
     * @param combatUser 이벤트를 호출한 플레이어
     * @param actionKey  동작 사용 키
     */
    public CombatUserActionEvent(CombatUser combatUser, ActionKey actionKey) {
        super(combatUser);
        this.actionKey = actionKey;
    }
}
