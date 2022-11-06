package com.dace.dmgr.combat.entity;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import static com.dace.dmgr.system.HashMapList.temporalEntityMap;

public class TemporalEntity<T extends LivingEntity> extends CombatEntity<T> {
    protected TemporalEntity(EntityType entityType, String name, Location location, Hitbox hitbox) {
        this(entityType, name, location, hitbox, null);
    }

    protected TemporalEntity(EntityType entityType, String name, Location location, Hitbox hitbox, Hitbox critHitbox) {
        super((T) location.getWorld().spawnEntity(location, entityType), name, hitbox, critHitbox);
        temporalEntityMap.put(getEntity(), this);
    }

    public void remove() {
        entity.setHealth(0);
        entity.remove();
    }
}
