package com.dace.dmgr.combat.entity;

import com.dace.dmgr.util.LocationUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

/**
 * 공격 판정에 사용하는 히트박스 클래스.
 */
@RequiredArgsConstructor
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

    public final Location getCenter() {
        return center.clone();
    }

    /**
     * 히트박스의 중앙 위치를 설정한다.
     *
     * @param location 중앙 위치
     */
    public final void setCenter(Location location) {
        this.center = location.clone();
        center.setPitch(0);
        center = LocationUtil.getLocationFromOffset(center, offsetX, offsetY, offsetZ);
    }
}
