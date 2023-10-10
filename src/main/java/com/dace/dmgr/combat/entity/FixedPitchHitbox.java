package com.dace.dmgr.combat.entity;

import com.dace.dmgr.util.LocationUtil;
import org.bukkit.Location;

/**
 * Pitch가 고정된 히트박스 클래스.
 */
public final class FixedPitchHitbox extends Hitbox {
    public FixedPitchHitbox(Location center, double sizeX, double sizeY, double sizeZ, double offsetX, double offsetY, double offsetZ) {
        super(center, sizeX, sizeY, sizeZ, offsetX, offsetY, offsetZ);
    }

    public FixedPitchHitbox(Location center, double sizeX, double sizeY, double sizeZ, double offsetX, double offsetY, double offsetZ, double axisOffsetX, double axisOffsetY, double axisOffsetZ) {
        super(center, sizeX, sizeY, sizeZ, offsetX, offsetY, offsetZ, axisOffsetX, axisOffsetY, axisOffsetZ);
    }

    @Override
    public void setCenter(Location location) {
        Location loc = location.clone();
        loc.setPitch(0);
        center = LocationUtil.getLocationFromOffset(loc, offsetX, offsetY, offsetZ)
                .add(axisOffsetX, axisOffsetY, axisOffsetZ);
    }
}
