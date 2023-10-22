package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.character.jager.action.JagerA1Entity;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public final class OnEntityTarget implements Listener {
    @EventHandler
    public static void event(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity))
            return;

        CombatEntity combatEntity = EntityInfoRegistry.getCombatEntity((LivingEntity) entity);

        if (combatEntity != null) {
            if (combatEntity instanceof JagerA1Entity) {
                event.setCancelled(true);
            }
        }
    }
}
