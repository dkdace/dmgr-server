package com.kiwi.dmgr.game.mode;

public abstract class GameMode implements IGameMode {

    /* 최대 수용 가능한 플레이어 수 */
    public final int maxPlayer;

    /* 팀전 유무 */
    public final boolean isVerseTeam;

    /* 플레이 타임 */
    public final int playTime;

    public GameMode(boolean isVerseTeam, int maxPlayer, int playTime) {
        this.isVerseTeam = isVerseTeam;
        this.maxPlayer = maxPlayer;
        this.playTime = playTime;
    }

    /*
    * 모든 게임 모드를 등록한다.
    * 서버를 열 때 이 함수를 작동하도록 해야한다.
    */
    public static void registerGameModes() {
        new TeamDeathMatch();
    }
}
