package com.dace.dmgr.system;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.combat.entity.TemporalEntity;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class HashMapList {
    public static HashMap<Player, User> userMap = new HashMap<>();
    public static HashMap<Player, CombatUser> combatUserMap = new HashMap<>();
    public static HashMap<Entity, ICombatEntity> combatEntityMap = new HashMap<>();
    public static HashMap<Entity, TemporalEntity<?>> temporalEntityMap = new HashMap<>();
}
