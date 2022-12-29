package com.dace.dmgr.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * 벡터 관련 기능을 제공하는 클래스.
 */
public class VectorUtil {
    /**
     * 위치의 방향을 기준으로 Roll 축을 반환한다.
     *
     * @param location 기준 위치
     * @return Roll 축
     */
    public static Vector getRollAxis(Location location) {
        return location.getDirection();
    }

    /**
     * 위치의 방향을 기준으로 Yaw 축을 반환한다.
     *
     * @param location 기준 위치
     * @return Yaw 축
     */
    public static Vector getYawAxis(Location location) {
        Location loc = location.clone();
        loc.setYaw(location.getYaw() + 90);
        loc.setPitch(0);
        return getRollAxis(location).getCrossProduct(loc.getDirection()).normalize();
    }

    /**
     * 위치의 방향을 기준으로 Pitch 축을 반환한다.
     *
     * @param location 기준 위치
     * @return Pitch 축
     */
    public static Vector getPitchAxis(Location location) {
        return getRollAxis(location).getCrossProduct(getYawAxis(location));
    }

    /**
     * 회전행렬을 이용하여 축을 기준으로 지정한 각도만큼 회전시킨 벡터를 반환한다.
     *
     * @param vector 대상 벡터
     * @param axis   기준 축
     * @param angle  각도
     * @return 최종 벡터
     */
    public static Vector getRotatedVector(Vector vector, Vector axis, double angle) {
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

    /**
     * 벡터의 성분을 지정한 값만큼 무작위로 분산시킨 벡터를 반환한다.
     *
     * @param vector 대상 벡터
     * @param amount 값
     * @return 최종 벡터
     */
    public static Vector getSpreadedVector(Vector vector, float amount) {
        Vector spread = Vector.getRandom().subtract(new Vector(0.5, 0.5, 0.5)).multiply(amount * vector.length() * 0.02);
        return vector.clone().add(spread);
    }
}
