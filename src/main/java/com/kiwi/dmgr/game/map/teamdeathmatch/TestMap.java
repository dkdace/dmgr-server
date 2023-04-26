package com.kiwi.dmgr.game.map.teamdeathmatch;

import com.kiwi.dmgr.game.map.GameMap;
import com.kiwi.dmgr.game.map.Point;
import com.kiwi.dmgr.game.mode.EnumGameMode;

public class TestMap extends GameMap {

    final double[] RED_SPAWN = new double[]{19.5, 63, 52.5, 180, 0};
    final double[] BLUE_SPAWN = new double[]{-114.5, 64, 53.5, -90, 0};

    public TestMap() {
        super(EnumGameMode.TeamDeathMatch, "테스트맵", "TestMap");
        addPointLocation(Point.RED_SPAWN, RED_SPAWN);
        addPointLocation(Point.BLUE_SPAWN, BLUE_SPAWN);
    }
}
