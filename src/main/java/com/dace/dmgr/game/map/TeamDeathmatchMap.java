package com.dace.dmgr.game.map;

import com.dace.dmgr.game.GamePlayMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 팀 데스매치의 맵 목록.
 */
@AllArgsConstructor
@Getter
public enum TeamDeathmatchMap implements GameMap {
    TESTMAP(GamePlayMode.TEAM_DEATHMATCH, "테스트맵", "TestMap",
            new GlobalLocation[]{new GlobalLocation(19.5, 63, 52.5, 180, 0)},
            new GlobalLocation[]{new GlobalLocation(-114.5, 64, 53.5, -90, 0)},
            new GlobalLocation[]{new GlobalLocation(1, 61, 21, 0, 0)}
    );

    /** 게임 모드 */
    private final GamePlayMode gamePlayMode;
    /** 맵 이름 */
    private final String name;
    /** 맵 월드 이름 */
    private final String worldName;
    /** 레드 팀 스폰 위치 목록 */
    private final GlobalLocation[] redTeamSpawns;
    /** 블루 팀 스폰 위치 목록 */
    private final GlobalLocation[] blueTeamSpawns;
    /** 힐 팩 위치 목록 */
    private final GlobalLocation[] healPackLocations;
}