package com.dace.dmgr.combat.entity;

import com.dace.dmgr.util.LocationUtil;
import org.bukkit.Location;

/**
 * 공격 판정에 사용하는 히트박스 클래스.
 */
public class Hitbox {
    /** 가로 */
    private final double sizeX;
    /** 높이 */
    private final double sizeY;
    /** 세로 */
    private final double sizeZ;
    /** 중앙 위치의 오프셋. 왼쪽(-) / 오른쪽(+) */
    private final double offsetX;
    /** 중앙 위치의 오프셋. 아래(-) / 위(+) */
    private final double offsetY;
    /** 중앙 위치의 오프셋. 뒤(-) / 앞(+) */
    private final double offsetZ;
    /** 중앙 위치 */
    private Location center;

    /**
     * 히트박스 인스턴스를 생성한다.
     *
     * @param center  중앙 위치
     * @param offsetX 중앙 위치의 오프셋. 왼쪽(-) / 오른쪽(+)
     * @param offsetY 중앙 위치의 오프셋. 아래(-) / 위(+)
     * @param offsetZ 중앙 위치의 오프셋. 뒤(-) / 앞(+)
     * @param sizeX   가로
     * @param sizeY   높이
     * @param sizeZ   세로
     */
    public Hitbox(Location center, double offsetX, double offsetY, double offsetZ, double sizeX, double sizeY, double sizeZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        setCenter(center);
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

    /**
     * 히트박스의 중앙 위치를 설정한다.
     *
     * @param location 중앙 위치
     */
    public void setCenter(Location location) {
        this.center = location.clone();
        location.setPitch(0);
        center = LocationUtil.getLocationFromOffset(center, offsetX, offsetY, offsetZ);
    }
}
