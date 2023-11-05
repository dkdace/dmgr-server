package com.dace.dmgr.game;

import com.dace.dmgr.game.scheduler.GameModeScheduler;
import com.dace.dmgr.game.scheduler.TeamDeathmatchScheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 게임 모드의 종류.
 */
@AllArgsConstructor
@Getter
public enum GameMode {
    TEAM_DEATHMATCH("팀 데스매치", 4, 10, false, 20, 10,
            TeamDeathmatchScheduler.getInstance());

    /** 이름 */
    private final String name;
    /** 게임을 시작하기 위한 최소 플레이어 수 */
    private final int minPlayer;
    /** 최대 수용 가능한 플레이어 수 */
    private final int maxPlayer;
    /** 랭크 여부 */
    private final boolean isRanked;
    /** 게임 준비 시간 */
    private final int readyDuration;
    /** 게임 진행 시간 */
    private final int playDuration;
    /** 게임 모드 스케쥴러 */
    private final GameModeScheduler gameModeScheduler;
}
