package com.dace.dmgr.game.map;

import com.dace.dmgr.util.location.GlobalLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * 팀 데스매치의 맵 목록.
 */
@AllArgsConstructor
@Getter
public enum TeamDeathmatchMap implements GameMap {
    TESTMAP("테스트맵", Bukkit.getWorld("TestMap"),
            new GlobalLocation[]{new GlobalLocation(19.5, 63, 52.5, 180, 0)},
            new GlobalLocation[]{new GlobalLocation(-114.5, 64, 53.5, -90, 0)});
//    MEDIEVAL("중세풍", Bukkit.getWorld("Medieval"),
//            new GlobalLocation[]{new GlobalLocation(0.5, 63, 126.5, 180, 0)},
//            new GlobalLocation[]{new GlobalLocation(1.0, 63, -126.5, 0, 0)});

    /** 맵 이름 */
    private final String name;
    /** 맵 월드 */
    private final World world;
    /** 레드 팀 스폰 위치 목록 */
    private final GlobalLocation[] redTeamSpawns;
    /** 블루 팀 스폰 위치 목록 */
    private final GlobalLocation[] blueTeamSpawns;
}