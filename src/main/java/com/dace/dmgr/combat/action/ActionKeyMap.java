package com.dace.dmgr.combat.action;

import java.util.EnumMap;
import java.util.HashMap;

public class ActionKeyMap {
    private final EnumMap<ActionKey, Action> actionMap = new EnumMap<>(ActionKey.class);
    private final HashMap<Action, ActionKey> keyMap = new HashMap<>();

    public EnumMap<ActionKey, Action> getAll() {
        return actionMap;
    }

    public Action get(ActionKey actionKey) {
        return actionMap.get(actionKey);
    }

    public ActionKey getKey(Action action) {
        return keyMap.get(action);
    }

    public ActionKeyMap set(ActionKey actionKey, Action action) {
        actionMap.put(actionKey, action);
        keyMap.put(action, actionKey);
        return this;
    }
}
