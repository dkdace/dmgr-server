package com.dace.dmgr.system;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.combat.entity.TemporalEntity;
import com.dace.dmgr.user.User;

import java.util.HashMap;
import java.util.UUID;

public class EntityList {
    public static HashMap<UUID, User> userList = new HashMap<UUID, User>();
    public static HashMap<UUID, CombatUser> combatUserList = new HashMap<>();
    public static HashMap<Integer, ICombatEntity> combatEntityList = new HashMap<>();
    public static HashMap<Integer, TemporalEntity<?>> temporalEntityList = new HashMap<>();
}
