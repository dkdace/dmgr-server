package com.dace.dmgr.combat.entity;

import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * 공격 판정에 사용하는 히트박스 클래스.
 *
 * @see FixedPitchHitbox
 */
public class Hitbox {
    /** 가로. 단위: 블록 */
    @Getter
    protected final double sizeX;
    /** 높이. 단위: 블록 */
    @Getter
    protected final double sizeY;
    /** 세로. 단위: 블록 */
    @Getter
    protected final double sizeZ;
    /** 중앙 위치 오프셋. 왼쪽(-) / 오른쪽(+) */
    @Getter
    protected final double offsetX;
    /** 중앙 위치 오프셋. 아래(-) / 위(+) */
    @Getter
    protected final double offsetY;
    /** 중앙 위치 오프셋. 뒤(-) / 앞(+) */
    @Getter
    protected final double offsetZ;
    /** 축 기준 중앙 위치 오프셋. -X / +X */
    @Getter
    protected final double axisOffsetX;
    /** 축 기준 중앙 위치 오프셋. -Y / +Y */
    @Getter
    protected final double axisOffsetY;
    /** 축 기준 중앙 위치 오프셋. -Z / +Z */
    @Getter
    protected final double axisOffsetZ;
    /** 중앙 위치 */
    protected Location center;

    /**
     * 히트박스 인스턴스를 생성한다.
     *
     * @param center  중앙 위치
     * @param sizeX   가로. 단위: 블록
     * @param sizeY   높이. 단위: 블록
     * @param sizeZ   세로. 단위: 블록
     * @param offsetX 중앙 위치 오프셋. 왼쪽(-) / 오른쪽(+)
     * @param offsetY 중앙 위치 오프셋. 아래(-) / 위(+)
     * @param offsetZ 중앙 위치 오프셋. 뒤(-) / 앞(+)
     */
    public Hitbox(Location center, double sizeX, double sizeY, double sizeZ, double offsetX, double offsetY, double offsetZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        axisOffsetX = 0;
        axisOffsetY = 0;
        axisOffsetZ = 0;
        setCenter(center);
    }

    /**
     * 히트박스 인스턴스를 생성한다.
     *
     * @param center      중앙 위치
     * @param sizeX       가로. 단위: 블록
     * @param sizeY       높이. 단위: 블록
     * @param sizeZ       세로. 단위: 블록
     * @param offsetX     중앙 위치 오프셋. 왼쪽(-) / 오른쪽(+)
     * @param offsetY     중앙 위치 오프셋. 아래(-) / 위(+)
     * @param offsetZ     중앙 위치 오프셋. 뒤(-) / 앞(+)
     * @param axisOffsetX 축 기준 중앙 오프셋. -X / +X
     * @param axisOffsetY 축 기준 중앙 위치 오프셋. 아래(-) / 위(+)
     * @param axisOffsetZ 축 기준 중앙 위치 오프셋. 뒤(-) / 앞(+)
     */
    public Hitbox(Location center, double sizeX, double sizeY, double sizeZ, double offsetX, double offsetY, double offsetZ, double axisOffsetX, double axisOffsetY, double axisOffsetZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.axisOffsetX = axisOffsetX;
        this.axisOffsetY = axisOffsetY;
        this.axisOffsetZ = axisOffsetZ;
        setCenter(center);
    }

    public final Location getCenter() {
        return center.clone();
    }

    /**
     * 히트박스의 중앙 위치를 설정한다.
     *
     * @param location 중앙 위치
     */
    public void setCenter(Location location) {
        center = LocationUtil.getLocationFromOffset(location.clone(), offsetX, offsetY, offsetZ)
                .add(axisOffsetX, axisOffsetY, axisOffsetZ);
    }

    /**
     * 지정한 위치까지의 거리를 반환한다.
     *
     * @param location 확인할 위치
     * @return 지정한 위치까지의 거리
     */
    public final double getDistance(Location location) {
        Vector rotVec = VectorUtil.getRotatedVector(
                VectorUtil.getRotatedVector(location.toVector().subtract(center.toVector()), new Vector(0, 1, 0), center.getYaw()),
                new Vector(1, 0, 0), center.getPitch());
        Location rotLoc = center.clone().add(rotVec);
        Location cuboidEdge = center.clone().add(
                (rotLoc.getX() > center.getX() ? 1 : -1) * Math.min(sizeX / 2, Math.abs(rotLoc.getX() - center.getX())),
                (rotLoc.getY() > center.getY() ? 1 : -1) * Math.min(sizeY / 2, Math.abs(rotLoc.getY() - center.getY())),
                (rotLoc.getZ() > center.getZ() ? 1 : -1) * Math.min(sizeZ / 2, Math.abs(rotLoc.getZ() - center.getZ()))
        );

        return cuboidEdge.distance(rotLoc);
    }

    /**
     * 지정한 위치의 구체가 히트박스와 접하고 있는 지 확인한다.
     *
     * @param location 확인할 위치
     * @param radius   판정 구체의 반지름
     * @return {@code location}이 히트박스의 내부에 있으면 {@code true} 반환
     */
    public final boolean isInHitbox(Location location, float radius) {
        return getDistance(location) <= radius;
    }
}
