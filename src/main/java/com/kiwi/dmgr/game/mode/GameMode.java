package com.kiwi.dmgr.game.mode;

public abstract class GameMode implements IGameMode {

    /* 최대 수용 가능한 플레이어 수 */
    public final int maxPlayer;

    /* 팀전 유무 */
    public final boolean isVerseTeam;

    /*  */
    public boolean isStartAble;

    protected GameMode(boolean isVerseTeam, int maxPlayer) {
        this.isVerseTeam = isVerseTeam;
        this.maxPlayer = maxPlayer;
    }
}
