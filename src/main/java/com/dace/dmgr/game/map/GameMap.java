package com.dace.dmgr.game.map;

import com.dace.dmgr.game.GamePlayMode;

/**
 * 게임에 사용되는 맵을 관리하는 인터페이스.
 */
public interface GameMap {
    /**
     * @return 게임 모드
     */
    GamePlayMode getGamePlayMode();

    /**
     * @return 맵 이름
     */
    String getName();

    /**
     * @return 월드 이름
     */
    String getWorldName();

    /**
     * @return 레드 팀 스폰 위치 목록
     */
    GlobalLocation[] getRedTeamSpawns();

    /**
     * @return 블루 팀 스폰 위치 목록
     */
    GlobalLocation[] getBlueTeamSpawns();
}