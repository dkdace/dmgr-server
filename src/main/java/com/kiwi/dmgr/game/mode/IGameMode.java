package com.kiwi.dmgr.game.mode;

import com.dace.dmgr.system.task.TaskTimer;
import com.kiwi.dmgr.game.Game;
import org.bukkit.scoreboard.Scoreboard;

/**
 * 게임 모드 인터페이스 클래스
 */
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
     * 스케쥴러 주기는 1초(20 Tick)로 한다.
     *
     * @param game 게임
     */
    void run(Game game);

    /**
     * 해당 게임모드 스코어보드를 리턴한다.
     *
     * @return 스코어보드

    Scoreboard getScoreboard();
    */
}
