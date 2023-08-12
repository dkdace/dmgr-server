package com.kiwi.dmgr.game.mode;

import lombok.Getter;

@Getter
public enum EnumGameMode {

    TeamDeathMatch("팀데스매치", 10, true, 600, new TeamDeathMatch());

    /* 이름 */
    private final String name;

    /* 최대 수용 가능한 플레이어 수 */
    private final int maxPlayer;

    /* 팀전 유무 */
    private final boolean isVerseTeam;

    /* 플레이 타임 */
    private final int playTime;

    /* 해당 게임 모드 인스턴스 */
    private final GameMode instance;

    EnumGameMode(String name, int maxPlayer, boolean isVerseTeam, int playTime, GameMode instance) {
        this.name = name;
        this.maxPlayer = maxPlayer;
        this.isVerseTeam = isVerseTeam;
        this.playTime = playTime;
        this.instance = instance;
    }
}
