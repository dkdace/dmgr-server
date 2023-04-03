package com.kiwi.dmgr.game.map;

import com.kiwi.dmgr.game.Team;
import com.kiwi.dmgr.game.mode.GameMode;
import lombok.Getter;
import org.bukkit.Location;

import java.util.HashMap;

/**
 * 게임 맵의 정보를 담는 클래스
 */
@Getter
public abstract class GameMap {

    /* 게임 모드 타입 */
    private final GameMode mode;

    /* 각 팀의 스폰 위치 */
    private final HashMap<Team, Location> teamSpawnLocation;

    protected GameMap(GameMode mode, HashMap<Team, Location> teamSpawnLocation) {
        this.mode = mode;
        this.teamSpawnLocation = teamSpawnLocation;
    }
}
