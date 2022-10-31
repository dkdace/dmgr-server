package com.dace.dmgr.combat.entity;

import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.system.task.TaskWait;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

import static com.dace.dmgr.system.HashMapList.combatEntityMap;

public class CombatEntity<T extends LivingEntity> implements ICombatEntity {
    protected final T entity;
    private final Hitbox hitbox;
    private String name;
    private String team = "";
    private int speedIncrement = 0;

    protected CombatEntity(T entity, String name, Hitbox hitbox) {
        this.entity = entity;
        this.hitbox = hitbox;
        this.name = name;
        combatEntityMap.put(entity, this);
    }

    @Override
    public T getEntity() {
        return entity;
    }

    @Override
    public void updateHitboxTick() {
        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (combatEntityMap.get(entity) == null)
                    return false;

                Location oldLoc = entity.getLocation();

                new TaskWait(2) {
                    @Override
                    public void run() {
                        hitbox.setLocation(oldLoc);
                    }
                };

                return true;
            }
        };
    }

    @Override
    public Hitbox getHitbox() {
        return hitbox;
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
        return (int) (Math.round(entity.getHealth() * 50 * 100) / 100);
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
