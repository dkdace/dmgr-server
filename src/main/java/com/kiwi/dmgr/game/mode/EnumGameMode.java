package com.kiwi.dmgr.game.mode;

import lombok.Getter;

@Getter
public enum EnumGameMode {

    TeamDeathMatch(10, true, 600, new TeamDeathMatch());

    /* 최대 수용 가능한 플레이어 수 */
    private final int maxPlayer;

    /* 팀전 유무 */
    private final boolean isVerseTeam;

    /* 플레이 타임 */
    private final int playTime;

    /* 해당 게임 모드 인스턴스 */
    private final GameMode instance;

    EnumGameMode(int maxPlayer, boolean isVerseTeam, int playTime, GameMode instance) {
        this.maxPlayer = maxPlayer;
        this.isVerseTeam = isVerseTeam;
        this.playTime = playTime;
        this.instance = instance;
    }
}
