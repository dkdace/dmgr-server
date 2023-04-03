package com.kiwi.dmgr.game.mode;

import lombok.Getter;

/**
 * 게임 모드의 정보를 담고 관리하는 클래스
 */
@Getter
public abstract class GameMode implements IGameMode {

    /* 최대 수용 가능한 플레이어 수 */
    private final int maxPlayer;

    /* 팀전 유무 */
    private final boolean isVerseTeam;

    /* 플레이 타임 */
    private final int playTime;

    public GameMode(boolean isVerseTeam, int maxPlayer, int playTime) {
        this.isVerseTeam = isVerseTeam;
        this.maxPlayer = maxPlayer;
        this.playTime = playTime;
    }
}
