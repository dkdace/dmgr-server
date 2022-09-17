package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.TemporalEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import static com.dace.dmgr.system.HashMapList.temporalEntityHashMap;

public class OnEntityDeath implements Listener {
    @EventHandler
    public static void event(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            TemporalEntity<?> temporalEntity = temporalEntityHashMap.get(entity);

            if (temporalEntity != null)
                temporalEntityHashMap.remove(entity);
        }
    }
}
