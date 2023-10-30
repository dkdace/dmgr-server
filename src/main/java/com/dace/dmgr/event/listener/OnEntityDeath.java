package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public final class OnEntityDeath implements Listener {
    @EventHandler
    public static void event(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        CombatEntity combatEntity = EntityInfoRegistry.getCombatEntity(entity);

        if (combatEntity != null)
            combatEntity.remove();
    }
}
