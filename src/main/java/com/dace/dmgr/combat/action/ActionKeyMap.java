package com.dace.dmgr.combat.action;

import java.util.EnumMap;
import java.util.HashMap;

/**
 * 상호작용 키 매핑 목록을 관리하는 클래스.
 *
 * @see ActionKey
 * @see ActionInfo
 */
public class ActionKeyMap {
    /** 상호작용 키와 상호작용 목록 */
    private final EnumMap<ActionKey, ActionInfo> actionMap = new EnumMap<>(ActionKey.class);
    /** 상호작용과 상호작용 키 목록 */
    private final HashMap<ActionInfo, ActionKey> keyMap = new HashMap<>();

    public EnumMap<ActionKey, ActionInfo> getAll() {
        return actionMap;
    }

    /**
     * 상호작용 키에 매핑된 상호작용을 반환한다.
     *
     * @param actionKey 상호작용 키
     * @return 상호작용
     */
    public ActionInfo get(ActionKey actionKey) {
        return actionMap.get(actionKey);
    }

    /**
     * 상호작용에 매핑된 상호작용 키를 반환한다.
     *
     * @param actionInfo 상호작용
     * @return 상호작용 키
     */
    public ActionKey getKey(ActionInfo actionInfo) {
        return keyMap.get(actionInfo);
    }

    /**
     * 상호작용 키 매핑 목록에 상호작용을 추가한다.
     *
     * @param actionKey 상호작용 키
     * @param actionInfo    상호작용
     * @return ActionKeyMap
     */
    public ActionKeyMap put(ActionKey actionKey, ActionInfo actionInfo) {
        actionMap.put(actionKey, actionInfo);
        keyMap.put(actionInfo, actionKey);
        return this;
    }
}
