package com.dace.dmgr.combat.entity;

import org.bukkit.Location;

public class Hitbox {
    private final double sizeX;
    private final double sizeY;
    private final double sizeZ;
    private Location center;

    public Hitbox(Location center, double sizeX, double sizeY, double sizeZ) {
        this.center = center;
        center.setPitch(0);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public double getSizeX() {
        return sizeX;
    }

    public double getSizeY() {
        return sizeY;
    }

    public double getSizeZ() {
        return sizeZ;
    }

    public Location getCenter() {
        return center.clone();
    }

    public void setCenter(Location center) {
        this.center = center;
        center.setPitch(0);
    }
}
