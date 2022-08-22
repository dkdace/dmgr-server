package com.dace.dmgr.combat.entity;

import org.bukkit.entity.LivingEntity;

public interface ICombatEntity {
    LivingEntity getEntity();

    String getTeam();

    void setTeam(String team);

    String getName();

    void setName(String name);

    int getHealth();

    void setHealth(int health);

    int getMaxHealth();

    void setMaxHealth(int health);
}
