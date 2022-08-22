package com.dace.dmgr.combat.entity;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import static com.dace.dmgr.system.EntityList.temporalEntityList;

public class TemporalEntity<T extends LivingEntity> extends CombatEntity<T> {
    protected TemporalEntity(EntityType entityType, String name, Location location) {
        super((T) location.getWorld().spawnEntity(location, entityType), name);
        temporalEntityList.put(entity.getEntityId(), this);
    }

    public void remove() {
        temporalEntityList.remove(entity.getEntityId());
        entity.remove();
    }
}
