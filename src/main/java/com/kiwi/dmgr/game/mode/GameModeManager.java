package com.kiwi.dmgr.game.mode;

public class GameModeManager {
    /*
     * 모든 게임 모드를 등록한다.
     * 서버를 열 때 이 함수를 작동하도록 해야한다.
     */
    public static void registerGameModes() {
        new TeamDeathMatch();
    }
}
