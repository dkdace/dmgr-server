package com.dace.dmgr.event;

import com.dace.dmgr.combat.entity.TemporalEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;

import static com.dace.dmgr.system.EntityList.temporalEntityList;

public class EntityDeath {
    public static void event(EntityDeathEvent event, Entity entity) {
        if (entity.getType() != EntityType.PLAYER) {
            TemporalEntity<?> temporalEntity = temporalEntityList.get(entity.getEntityId());

            if (temporalEntity != null)
                temporalEntityList.remove(entity.getEntityId());
        }
    }
}
