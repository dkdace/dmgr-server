package com.dace.dmgr.game;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.game.map.TeamDeathmatchMap;
import com.dace.dmgr.game.scheduler.GamePlayModeScheduler;
import com.dace.dmgr.game.scheduler.TeamDeathmatchScheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;

/**
 * 게임 모드의 종류.
 */
@AllArgsConstructor
@Getter
public enum GamePlayMode {
    TEAM_DEATHMATCH("팀 데스매치", 4, 10, TeamDeathmatchMap.values(), false, 20, 600,
            TeamDeathmatchScheduler.getInstance());

    /** 이름 */
    private final String name;
    /** 최소 플레이어 수 */
    private final int minPlayer;
    /** 최대 플레이어 수 */
    private final int maxPlayer;
    /** 게임 맵 목록 */
    private final GameMap[] gameMaps;
    /** 랭크 여부 */
    private final boolean isRanked;
    /** 게임 준비 시간 (초) */
    private final int readyDuration;
    /** 게임 진행 시간 (초) */
    private final int playDuration;
    /** 게임 모드 스케쥴러 */
    private final GamePlayModeScheduler gamePlayModeScheduler;

    /**
     * 일반 또는 랭크 게임의 무작위 게임 모드를 반환한다.
     *
     * @param isRanked 랭크 여부
     * @return 무작위 게임 모드
     */
    @NonNull
    public static GamePlayMode getRandomGamePlayMode(boolean isRanked) {
        GamePlayMode[] gamePlayModes = Arrays.stream(values())
                .filter(gamePlayMode -> isRanked == gamePlayMode.isRanked()).toArray(GamePlayMode[]::new);

        int index = DMGR.getRandom().nextInt(gamePlayModes.length);
        return gamePlayModes[index];
    }

    /**
     * 게임 모드의 무작위 맵을 반환한다.
     *
     * @return 무작위 맵
     */
    @NonNull
    public GameMap getRandomMap() {
        int index = DMGR.getRandom().nextInt(gameMaps.length);
        return gameMaps[index];
    }
}
