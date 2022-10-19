package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.combat.entity.TemporalEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import static com.dace.dmgr.system.HashMapList.combatEntityMap;
import static com.dace.dmgr.system.HashMapList.temporalEntityMap;

public class OnEntityDeath implements Listener {
    @EventHandler
    public static void event(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            ICombatEntity combatEntity = combatEntityMap.get(entity);

            if (combatEntity instanceof TemporalEntity) {
                combatEntityMap.remove(entity);
                temporalEntityMap.remove(entity);
            }
        }
    }
}
