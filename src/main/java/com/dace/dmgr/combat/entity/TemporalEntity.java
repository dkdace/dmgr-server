package com.dace.dmgr.combat.entity;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import static com.dace.dmgr.system.HashMapList.temporalEntityMap;

public class TemporalEntity<T extends LivingEntity> extends CombatEntity<T> {
    protected TemporalEntity(EntityType entityType, String name, Location location) {
        super((T) location.getWorld().spawnEntity(location, entityType), name);
        temporalEntityMap.put(getEntity(), this);
    }

    public void remove() {
        entity.remove();
    }
}
