package com.dace.dmgr.event;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class EntityDamageByEntity {
    public static void event(EntityDamageByEntityEvent event, Entity attacker, Entity victim) {
        if (attacker.getType() == EntityType.PLAYER) {
            CombatUser attCombatUser = combatUserList.get(attacker.getUniqueId());

            if (attCombatUser.getCharacter() != null)
                event.setCancelled(true);
        }
    }
}

