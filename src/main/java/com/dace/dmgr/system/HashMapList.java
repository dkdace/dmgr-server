package com.dace.dmgr.system;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.combat.entity.TemporalEntity;
import com.dace.dmgr.lobby.User;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * 엔티티 저장용 {@link HashMap} 목록.
 *
 * <p>대상 엔티티가 생성될 때 추가되어야 하며, 엔티티가 죽거나 소멸될 때 제거되어야 한다.</p>
 */
public class HashMapList {
    public static HashMap<Player, User> userMap = new HashMap<>();
    public static HashMap<Player, CombatUser> combatUserMap = new HashMap<>();
    public static HashMap<Entity, ICombatEntity> combatEntityMap = new HashMap<>();
    public static HashMap<Entity, TemporalEntity<?>> temporalEntityMap = new HashMap<>();
}
