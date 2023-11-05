package com.dace.dmgr.game.map;

import com.dace.dmgr.game.GameMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * 팀 데스매치의 맵 목록.
 */
@AllArgsConstructor
@Getter
public enum TeamDeathmatchMap implements GameMap {
    TESTMAP(GameMode.TEAM_DEATHMATCH, "테스트맵", "TestMap",
            new Location[]{new Location(Bukkit.getWorld("TestMap"), 19.5, 63, 52.5, 180, 0)},
            new Location[]{new Location(Bukkit.getWorld("TestMap"), -114.5, 64, 53.5, -90, 0)});

    /** 게임 모드 */
    private final GameMode gameMode;
    /** 맵 이름 */
    private final String name;
    /** 맵 월드 이름 */
    private final String worldName;
    /** 레드 팀 스폰 위치 목록 */
    private final Location[] redTeamSpawns;
    /** 블루 팀 스폰 위치 목록 */
    private final Location[] blueTeamSpawns;
}