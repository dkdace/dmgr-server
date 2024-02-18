package com.dace.dmgr.game.map;

import com.dace.dmgr.game.GamePlayMode;
import lombok.NonNull;

/**
 * 게임에 사용되는 맵을 관리하는 인터페이스.
 */
public interface GameMap {
    /**
     * @return 게임 모드
     */
    @NonNull
    GamePlayMode getGamePlayMode();

    /**
     * @return 맵 이름
     */
    @NonNull
    String getName();

    /**
     * @return 월드 이름
     */
    @NonNull
    String getWorldName();

    /**
     * @return 레드 팀 스폰 위치 목록
     */
    @NonNull
    GlobalLocation @NonNull [] getRedTeamSpawns();

    /**
     * @return 블루 팀 스폰 위치 목록
     */
    GlobalLocation @NonNull [] getBlueTeamSpawns();

    /**
     * @return 힐 팩 위치 목록
     */
    GlobalLocation @NonNull [] getHealPackLocations();
}
