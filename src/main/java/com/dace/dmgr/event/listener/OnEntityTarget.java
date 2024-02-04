package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public final class OnEntityTarget implements Listener {
    @EventHandler
    public static void event(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        CombatEntity combatEntity = CombatEntity.fromEntity(entity);

        if (combatEntity != null)
            event.setCancelled(true);
    }
}
