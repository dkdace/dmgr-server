package com.dace.dmgr.combat.entity;

import org.bukkit.entity.Entity;

public interface ICombatEntity {
    Entity getEntity();

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
}
