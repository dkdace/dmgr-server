package com.kiwi.dmgr.game.map;

import com.kiwi.dmgr.game.mode.EnumGameMode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

/**
 * 게임 맵의 정보를 담는 클래스
 */
@Getter
public abstract class GameMap {

    /* 게임 모드 타입 */
    private final EnumGameMode mode;

    /* 맵 이름 */
    private final String name;

    /* 맵 월드 이름 */
    private final String worldName;

    @Setter
    /* 맵 주요 위치
    *
    * <p> index 0, 1, 2에는 x, y, z
    *     index 3, 4에는 yaw, pitch </p>
    * */
    private HashMap<Point, double[]> pointLocation;

    public GameMap(EnumGameMode mode, String name, String worldName) {
        this.mode = mode;
        this.name = name;
        this.worldName = worldName;
        this.pointLocation = new HashMap<>();
    }

    public void addPointLocation(Point point, double[] location) {
        this.pointLocation.put(point, location);
    }
}
