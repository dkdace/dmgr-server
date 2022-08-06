package com.dace.dmgr.combat;

public interface CombatEntity {
    String getTeam();

    void setTeam(String team);

    int getHealth();

    void setHealth(int health);

    int getMaxHealth();

    void setMaxHealth(int health);
}
