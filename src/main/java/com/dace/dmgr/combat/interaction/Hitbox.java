package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * 공격 판정에 사용하는 히트박스 클래스.
 *
 * @see FixedPitchHitbox
 */
public class Hitbox {
    /** 가로. (단위: 블록) */
    @Getter
    @Setter
    protected double sizeX;
    /** 높이. (단위: 블록) */
    @Getter
    @Setter
    protected double sizeY;
    /** 세로. (단위: 블록) */
    @Getter
    @Setter
    protected double sizeZ;
    /** 중앙 위치 오프셋. 왼쪽(-) / 오른쪽(+). (단위 : 블록) */
    @Getter
    @Setter
    protected double offsetX;
    /** 중앙 위치 오프셋. 아래(-) / 위(+). (단위 : 블록) */
    @Getter
    @Setter
    protected double offsetY;
    /** 중앙 위치 오프셋. 뒤(-) / 앞(+). (단위 : 블록) */
    @Getter
    @Setter
    protected double offsetZ;
    /** 축 기준 중앙 위치 오프셋. -X / +X. (단위 : 블록) */
    @Getter
    @Setter
    protected double axisOffsetX;
    /** 축 기준 중앙 위치 오프셋. -Y / +Y. (단위 : 블록) */
    @Getter
    @Setter
    protected double axisOffsetY;
    /** 축 기준 중앙 위치 오프셋. -Z / +Z. (단위 : 블록) */
    @Getter
    @Setter
    protected double axisOffsetZ;
    /** 중앙 위치 */
    protected Location center;

    /**
     * 히트박스 인스턴스를 생성한다.
     *
     * @param location 기준 위치
     * @param sizeX    가로. (단위: 블록)
     * @param sizeY    높이. (단위: 블록)
     * @param sizeZ    세로. (단위: 블록)
     * @param offsetX  중앙 위치 오프셋. 왼쪽(-) / 오른쪽(+). (단위 : 블록)
     * @param offsetY  중앙 위치 오프셋. 아래(-) / 위(+). (단위 : 블록)
     * @param offsetZ  중앙 위치 오프셋. 뒤(-) / 앞(+). (단위 : 블록)
     */
    public Hitbox(@NonNull Location location, double sizeX, double sizeY, double sizeZ, double offsetX, double offsetY, double offsetZ) {
        this(location, sizeX, sizeY, sizeZ, offsetX, offsetY, offsetZ, 0, 0, 0);
    }

    /**
     * 히트박스 인스턴스를 생성한다.
     *
     * @param location    기존 위치
     * @param sizeX       가로. (단위: 블록)
     * @param sizeY       높이. (단위: 블록)
     * @param sizeZ       세로. (단위: 블록)
     * @param offsetX     중앙 위치 오프셋. 왼쪽(-) / 오른쪽(+). (단위 : 블록)
     * @param offsetY     중앙 위치 오프셋. 아래(-) / 위(+). (단위 : 블록)
     * @param offsetZ     중앙 위치 오프셋. 뒤(-) / 앞(+). (단위 : 블록)
     * @param axisOffsetX 축 기준 중앙 오프셋. -X / +X. (단위 : 블록)
     * @param axisOffsetY 축 기준 중앙 위치 오프셋. 아래(-) / 위(+). (단위 : 블록)
     * @param axisOffsetZ 축 기준 중앙 위치 오프셋. 뒤(-) / 앞(+). (단위 : 블록)
     */
    public Hitbox(@NonNull Location location, double sizeX, double sizeY, double sizeZ, double offsetX, double offsetY, double offsetZ,
                  double axisOffsetX, double axisOffsetY, double axisOffsetZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.axisOffsetX = axisOffsetX;
        this.axisOffsetY = axisOffsetY;
        this.axisOffsetZ = axisOffsetZ;
        setCenter(location);
    }

    @NonNull
    public final Location getCenter() {
        return center.clone();
    }

    /**
     * 히트박스의 중앙 위치를 설정한다.
     *
     * @param location 기준 위치
     */
    public void setCenter(@NonNull Location location) {
        center = LocationUtil.getLocationFromOffset(location.clone(), offsetX, offsetY, offsetZ)
                .add(axisOffsetX, axisOffsetY, axisOffsetZ);
    }

    /**
     * 히트박스 안에서 지정한 위치까지 가장 가까운 위치를 반환한다.
     *
     * @param location 확인할 위치
     * @return 가장 가까운 위치
     */
    @NonNull
    public final Location getNearestLocation(@NonNull Location location) {
        Vector rotVec = VectorUtil.getRotatedVector(
                VectorUtil.getRotatedVector(location.toVector().subtract(center.toVector()), new Vector(0, 1, 0), center.getYaw()),
                new Vector(1, 0, 0), center.getPitch());
        Location rotLoc = center.clone().add(rotVec);
        Location cuboidEdge = center.clone().add(
                (rotLoc.getX() > center.getX() ? 1 : -1) * Math.min(sizeX / 2, Math.abs(rotLoc.getX() - center.getX())),
                (rotLoc.getY() > center.getY() ? 1 : -1) * Math.min(sizeY / 2, Math.abs(rotLoc.getY() - center.getY())),
                (rotLoc.getZ() > center.getZ() ? 1 : -1) * Math.min(sizeZ / 2, Math.abs(rotLoc.getZ() - center.getZ()))
        );

        Vector retVec = VectorUtil.getRotatedVector(
                VectorUtil.getRotatedVector(cuboidEdge.toVector().subtract(center.toVector()), new Vector(1, 0, 0), -center.getPitch()),
                new Vector(0, 1, 0), -center.getYaw());
        return center.clone().add(retVec);
    }

    /**
     * 지정한 위치까지의 거리를 반환한다.
     *
     * @param location 확인할 위치
     * @return 지정한 위치까지의 거리. (단위: 블록)
     */
    public final double getDistance(@NonNull Location location) {
        return getNearestLocation(location).distance(location);
    }

    /**
     * 지정한 위치의 구체가 히트박스와 접하고 있는 지 확인한다.
     *
     * @param location 확인할 위치
     * @param radius   판정 구체의 반지름. (단위: 블록)
     * @return {@code location}이 히트박스의 내부에 있으면 {@code true} 반환
     */
    public final boolean isInHitbox(Location location, double radius) {
        return getDistance(location) <= radius;
    }
}
