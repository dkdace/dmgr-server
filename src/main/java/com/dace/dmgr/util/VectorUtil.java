package com.dace.dmgr.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorUtil {
    public static Vector getRollAxis(Location location) {
        return location.getDirection();
    }

    public static Vector getYawAxis(Location location) {
        Location loc = location.clone();
        loc.setYaw(location.getYaw() + 90);
        loc.setPitch(0);
        return getRollAxis(location).getCrossProduct(loc.getDirection()).normalize();
    }

    public static Vector getPitchAxis(Location location) {
        return getRollAxis(location).getCrossProduct(getYawAxis(location));
    }

    public static Vector spread(Vector vector, int amount) {
        Vector spread = Vector.getRandom().subtract(new Vector(0.5, 0.5, 0.5)).multiply(amount * vector.length() * 0.02);
        return vector.add(spread);
    }
}
