package com.dace.dmgr.combat.event.combatuser;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.CombatUserEvent;

/**
 * 플레이어가 상호작용할 시 호출되는 이벤트.
 *
 * <p>상호작용은 좌클릭, 우클릭 등을 말하며 종류는 {@link ActionKey}에 있다.</p>
 */
public class CombatUserActionEvent extends CombatUserEvent {
    /** 상호작용 종류 */
    private final ActionKey actionKey;

    /**
     * 이벤트를 생성한다.
     *
     * @param combatUser 이벤트를 호출한 플레이어
     * @param actionKey  상호작용 종류
     */
    public CombatUserActionEvent(CombatUser combatUser, ActionKey actionKey) {
        super(combatUser);
        this.actionKey = actionKey;
    }

    public ActionKey getActionKey() {
        return actionKey;
    }
}
