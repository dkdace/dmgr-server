package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.TemporalEntity;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public final class OnEntityDeath implements Listener {
    @EventHandler
    public static void event(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            CombatEntity<?> combatEntity = EntityInfoRegistry.getCombatEntity(entity);

            if (combatEntity instanceof TemporalEntity) {
                EntityInfoRegistry.removeCombatEntity(entity);
                EntityInfoRegistry.removeTemporalEntity(entity);
            }
        }
    }
}
