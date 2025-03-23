package com.dace.dmgr.game.mode;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.game.map.TeamDeathmatchMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.RandomUtils;

import java.util.Arrays;
import java.util.function.Function;

/**
 * 게임 모드의 종류.
 */
@AllArgsConstructor
public enum GamePlayMode {
    /** 팀 데스매치 */
    TEAM_DEATHMATCH("팀 데스매치", 4, 10, TeamDeathmatchMap.values(), false, Timespan.ofSeconds(20), Timespan.ofMinutes(10),
            TeamDeathmatchScheduler::new);

    /** 이름 */
    @NonNull
    @Getter
    private final String name;
    /** 최소 플레이어 수 */
    @Getter
    private final int minPlayer;
    /** 최대 플레이어 수 */
    @Getter
    private final int maxPlayer;
    /** 게임 맵 목록 */
    private final GameMap[] gameMaps;
    /** 랭크 여부 */
    @Getter
    private final boolean isRanked;
    /** 게임 준비 시간 */
    @Getter
    private final Timespan readyDuration;
    /** 게임 진행 시간 */
    @Getter
    private final Timespan playDuration;
    /** 게임 모드 스케쥴러 반환에 실행할 작업 */
    private final Function<Game, GamePlayModeScheduler> onGetScheduler;

    /**
     * 일반 또는 랭크 게임의 무작위 게임 모드를 반환한다.
     *
     * @param isRanked 랭크 여부
     * @return 무작위 게임 모드
     */
    @NonNull
    public static GamePlayMode getRandomGamePlayMode(boolean isRanked) {
        GamePlayMode[] gamePlayModes = Arrays.stream(values())
                .filter(gamePlayMode -> isRanked == gamePlayMode.isRanked())
                .toArray(GamePlayMode[]::new);

        return gamePlayModes[RandomUtils.nextInt(0, gamePlayModes.length)];
    }

    /**
     * 게임 모드 스케쥴러 인스턴스를 생성하여 반환한다.
     *
     * @param game 대상 게임
     * @return 게임 모드 스케쥴러
     */
    @NonNull
    public GamePlayModeScheduler createScheduler(@NonNull Game game) {
        return onGetScheduler.apply(game);
    }

    /**
     * 게임 모드의 무작위 맵을 반환한다.
     *
     * @return 무작위 맵
     */
    @NonNull
    public GameMap getRandomMap() {
        return gameMaps[RandomUtils.nextInt(0, gameMaps.length)];
    }
}
