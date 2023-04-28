package com.dace.dmgr.combat.entity;

import com.dace.dmgr.util.LocationUtil;
import lombok.Getter;
import org.bukkit.Location;

/**
 * 공격 판정에 사용하는 히트박스 클래스.
 */
public class Hitbox {
    /** 가로. 단위: 블록 */
    @Getter
    private final double sizeX;
    /** 높이. 단위: 블록 */
    @Getter
    private final double sizeY;
    /** 세로. 단위: 블록 */
    @Getter
    private final double sizeZ;
    /** 위치 오프셋. 왼쪽(-) / 오른쪽(+) */
    private final double offsetX;
    /** 위치 오프셋. 아래(-) / 위(+) */
    private final double offsetY;
    /** 위치 오프셋. 뒤(-) / 앞(+) */
    private final double offsetZ;
    /** 중앙 위치 */
    private Location center;

    /**
     * 히트박스 인스턴스를 생성한다.
     *
     * @param offsetX 위치 오프셋. 왼쪽(-) / 오른쪽(+)
     * @param offsetY 위치 오프셋. 아래(-) / 위(+)
     * @param offsetZ 위치 오프셋. 뒤(-) / 앞(+)
     * @param sizeX   가로
     * @param sizeY   높이
     * @param sizeZ   세로
     */
    public Hitbox(double offsetX, double offsetY, double offsetZ, double sizeX, double sizeY, double sizeZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
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
