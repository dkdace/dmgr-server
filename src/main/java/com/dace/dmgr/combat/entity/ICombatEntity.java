package com.dace.dmgr.combat.entity;

import org.bukkit.entity.Entity;

public interface ICombatEntity {
    Entity getEntity();

    void updateHitboxTick();

    Hitbox getHitbox();

    String getTeam();

    void setTeam(String team);

    String getName();

    void setName(String name);

    int getHealth();

    void setHealth(int health);

    int getMaxHealth();

    void setMaxHealth(int health);

    int getSpeedIncrement();

    void addSpeedIncrement(int speedIncrement);

    default boolean isUltChargeable() {
        return false;
    }

    default boolean isDamageable() {
        return true;
    }
}
