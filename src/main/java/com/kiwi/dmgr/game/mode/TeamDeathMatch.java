package com.kiwi.dmgr.game.mode;

import com.dace.dmgr.system.task.TaskTimer;
import com.kiwi.dmgr.game.Game;

/**
 * 팀데스매치 클래스
 */
public class TeamDeathMatch extends GameMode implements IGameMode {

    private static final boolean VERSETEAM = true;
    private static final int MAXPLAYER = 10;

    public TeamDeathMatch() {
        super(VERSETEAM, MAXPLAYER);
    }

    @Override
    public boolean isStartAble(int playerCount) {
        return playerCount > 0 && playerCount % 2 == 0;
    }

    @Override
    public void run(Game game) {
        new TaskTimer(20) {

            @Override
            public boolean run(int i) {
                return false;
            }

        };
    }
}
