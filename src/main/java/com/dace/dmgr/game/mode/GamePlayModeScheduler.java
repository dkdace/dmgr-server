package com.dace.dmgr.game.mode;

import com.dace.dmgr.game.map.GameMap;

/**
 * 게임 모드의 스케쥴러를 관리하는 인터페이스.
 */
public interface GamePlayModeScheduler {
    /**
     * 게임 진행 시 실행할 작업.
     */
    void onPlay();

    /**
     * 게임 진행 중 매 초마다 실행할 작업.
     *
     * @param remainingSeconds 게임 종료까지 남은 시간 (초)
     */
    void onSecond(int remainingSeconds);

    /**
     * 현재 팀 스폰 위치의 인덱스를 반환한다.
     *
     * @return 스폰 위치 배열 인덱스
     * @see GameMap#getRedTeamSpawns()
     * @see GameMap#getBlueTeamSpawns()
     */
    int getTeamSpawnIndex();
}
