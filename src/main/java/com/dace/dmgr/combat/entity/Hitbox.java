package com.dace.dmgr.combat.entity;

import com.dace.dmgr.util.LocationUtil;
import org.bukkit.Location;

public class Hitbox {
    private final double sizeX;
    private final double sizeY;
    private final double sizeZ;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private Location center;

    public Hitbox(Location location, double offsetX, double offsetY, double offsetZ, double sizeX, double sizeY, double sizeZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        setCenter(location);
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

    private void setCenter(Location location) {
        this.center = location.clone();
        location.setPitch(0);
        center = LocationUtil.setRelativeOffset(center, offsetX, offsetY, offsetZ);
    }

    public void setLocation(Location location) {
        setCenter(location);
    }
}
