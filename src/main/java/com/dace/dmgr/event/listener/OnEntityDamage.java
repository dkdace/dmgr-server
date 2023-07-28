package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class OnEntityDamage implements Listener {
    @EventHandler
    public static void event(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            CombatUser combatUser = EntityInfoRegistry.getCombatUser((Player) entity);

            if (combatUser != null) {
                switch (event.getCause()) {
                    case FALL:
                    case POISON:
                    case WITHER:
                    case SUFFOCATION:
                    case ENTITY_EXPLOSION:
                    case DROWNING:
                    case FIRE_TICK:
                        event.setCancelled(true);
                }
            }

            ((Player) entity).setNoDamageTicks(1);
            ((Player) entity).setMaximumNoDamageTicks(1);
        }
    }
}
