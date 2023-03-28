package com.kiwi.dmgr.game.mode;

import com.dace.dmgr.system.task.TaskTimer;
import com.kiwi.dmgr.game.Game;

public interface IGameMode {

    /**
     * 해당 게임 타입으로서 시작 가능 여부를 리턴한다.
     *
     * @param playerCount 플레이어 수
     * @return 시작 가능 여부
     */
    boolean isStartAble(int playerCount);

    /**
     * 해당 게임모드 스케쥴러를 실행한다.
     *
     * @param game 게임
     */
    void run(Game game);
}
