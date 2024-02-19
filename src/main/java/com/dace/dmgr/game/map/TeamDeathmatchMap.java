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
            new GlobalLocation[]{
                    new GlobalLocation(1, 61, 21),
                    new GlobalLocation(43, 61, -21),
                    new GlobalLocation(-4, 66, -15),
                    new GlobalLocation(-13, 61, -56),
                    new GlobalLocation(2, 66, -56),
                    new GlobalLocation(-47, 65, -52),
                    new GlobalLocation(-43, 66, 31),
                    new GlobalLocation(-60, 59, -4),
                    new GlobalLocation(-53, 67, -13),
                    new GlobalLocation(-82, 61, 36),
                    new GlobalLocation(-75, 63, 58)
            }
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