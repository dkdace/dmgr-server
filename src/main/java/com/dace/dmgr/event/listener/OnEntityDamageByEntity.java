package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class OnEntityDamageByEntity implements Listener {
    @EventHandler
    public static void event(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity victim = event.getEntity();

        if (attacker.getType() == EntityType.PLAYER) {
            CombatUser attCombatUser = combatUserList.get(attacker.getUniqueId());

            if (attCombatUser.getCharacter() != null)
                event.setCancelled(true);
        }
    }
}

