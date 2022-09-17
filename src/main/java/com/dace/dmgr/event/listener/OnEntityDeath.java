package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.TemporalEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import static com.dace.dmgr.system.EntityList.temporalEntityList;

public class OnEntityDeath implements Listener {
    @EventHandler
    public static void event(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (entity.getType() != EntityType.PLAYER) {
            TemporalEntity<?> temporalEntity = temporalEntityList.get(entity.getEntityId());

            if (temporalEntity != null)
                temporalEntityList.remove(entity.getEntityId());
        }
    }
}
