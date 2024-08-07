package com.dace.dmgr.game.scheduler;

import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.map.GameMap;
import lombok.NonNull;

/**
 * 게임 모드의 스케쥴러를 관리하는 인터페이스.
 */
public interface GamePlayModeScheduler {
    /**
     * 게임 진행 중 매 초마다 실행할 작업.
     *
     * @param game 해당 게임
     */
    void onSecond(@NonNull Game game);

    /**
     * 현재 레드 팀 스폰 위치의 인덱스.
     *
     * @return 배열 인덱스
     * @see GameMap#getRedTeamSpawns()
     */
    int getRedTeamSpawnIndex();

    /**
     * 현재 블루 팀 스폰 위치의 인덱스.
     *
     * @return 배열 인덱스
     * @see GameMap#getBlueTeamSpawns()
     */
    int getBlueTeamSpawnIndex();
}
