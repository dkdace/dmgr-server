package com.dace.dmgr.game;

import com.dace.dmgr.game.scheduler.GamePlayModeScheduler;
import com.dace.dmgr.game.scheduler.TeamDeathmatchScheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 게임 모드의 종류.
 */
@AllArgsConstructor
@Getter
public enum GamePlayMode {
    TEAM_DEATHMATCH("팀 데스매치", 4, 10, false, 20, 600,
            TeamDeathmatchScheduler.getInstance());

    /** 이름 */
    private final String name;
    /** 최소 플레이어 수 */
    private final int minPlayer;
    /** 최대 플레이어 수 */
    private final int maxPlayer;
    /** 랭크 여부 */
    private final boolean isRanked;
    /** 게임 준비 시간 */
    private final int readyDuration;
    /** 게임 진행 시간 */
    private final int playDuration;
    /** 게임 모드 스케쥴러 */
    private final GamePlayModeScheduler gamePlayModeScheduler;
}
