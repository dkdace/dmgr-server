package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class OnEntityDamageByEntity implements Listener {
    @EventHandler
    public static void event(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();

        if (attacker instanceof Player) {
            CombatUser attCombatUser = EntityInfoRegistry.getCombatUser((Player) attacker);

            if (attCombatUser != null && attCombatUser.getCharacter() != null)
                event.setCancelled(true);
        }
    }
}

