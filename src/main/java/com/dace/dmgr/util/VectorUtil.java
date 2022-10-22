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

    public static Vector rotate(Vector vector, Vector axis, double angle) {
        double sin = Math.sin(Math.toRadians(angle));
        double cos = Math.cos(Math.toRadians(angle));
        Vector finalAxis = axis.clone().normalize();
        double ax = finalAxis.getX();
        double ay = finalAxis.getY();
        double az = finalAxis.getZ();

        Vector rotX = new Vector(cos + ax * ax * (1.0 - cos), ax * ay * (1.0 - cos) - az * sin, ax * az * (1.0 - cos) + ay * sin);
        Vector rotY = new Vector(ay * ax * (1.0 - cos) + az * sin, cos + ay * ay * (1.0 - cos), ay * az * (1.0 - cos) - ax * sin);
        Vector rotZ = new Vector(az * ax * (1.0 - cos) - ay * sin, az * ay * (1.0 - cos) + ax * sin, cos + az * az * (1.0 - cos));
        double x = rotX.dot(vector);
        double y = rotY.dot(vector);
        double z = rotZ.dot(vector);

        return new Vector(x, y, z);
    }

    public static Vector spread(Vector vector, float amount) {
        Vector spread = Vector.getRandom().subtract(new Vector(0.5, 0.5, 0.5)).multiply(amount * vector.length() * 0.02);
        return vector.clone().add(spread);
    }
}
