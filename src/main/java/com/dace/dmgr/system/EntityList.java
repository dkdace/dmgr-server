package com.dace.dmgr.system;

import com.dace.dmgr.user.User;
import com.dace.dmgr.combat.CombatUser;

import java.util.HashMap;
import java.util.UUID;

public class EntityList {
    public static HashMap<UUID, User> userList = new HashMap<UUID, User>();
    public static HashMap<UUID, CombatUser> combatUserList = new HashMap<>();
}
