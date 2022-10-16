package com.dace.dmgr.combat.entity;

import org.bukkit.Location;

public class Hitbox {
    private final ICombatEntity combatEntity;
    private final double width;
    private final double height;
    private Location location;

    public Hitbox(ICombatEntity combatEntity, double width, double height) {
        this.combatEntity = combatEntity;
        this.location = combatEntity.getEntity().getLocation();
        this.width = width;
        this.height = height;
    }

    public ICombatEntity getCombatEntity() {
        return combatEntity;
    }

    public Location getLocation() {
        return location.clone();
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
