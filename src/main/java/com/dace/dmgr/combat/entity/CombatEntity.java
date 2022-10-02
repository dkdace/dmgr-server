package com.dace.dmgr.combat.entity;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

public class CombatEntity<T extends LivingEntity> implements ICombatEntity {
    protected final T entity;
    private String name;
    private String team = "";
    private int speedIncrement = 0;

    protected CombatEntity(T entity, String name) {
        this.entity = entity;
        this.name = name;
    }

    @Override
    public T getEntity() {
        return entity;
    }

    @Override
    public String getTeam() {
        return team;
    }

    @Override
    public void setTeam(String team) {
        this.team = team;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        entity.setCustomName(name);
        this.name = name;
    }

    @Override
    public int getHealth() {
        return (int) (entity.getHealth() * 50);
    }

    @Override
    public void setHealth(int health) {
        if (health < 0) health = 0;
        if (health > getMaxHealth()) health = getMaxHealth();
        double realHealth = health / 50.0;
        entity.setHealth(realHealth);
    }

    @Override
    public int getMaxHealth() {
        return (int) (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 50);
    }

    @Override
    public void setMaxHealth(int health) {
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health / 50.0);
    }

    @Override
    public int getSpeedIncrement() {
        return speedIncrement;
    }

    @Override
    public void addSpeedIncrement(int speedIncrement) {
        this.speedIncrement += speedIncrement;
        if (this.speedIncrement < -100) this.speedIncrement = -100;
        if (this.speedIncrement > 100) this.speedIncrement = 100;
    }
}
