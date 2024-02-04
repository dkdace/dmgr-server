package com.dace.dmgr.game;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.game.map.TeamDeathmatchMap;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;

/**
 * 게임 시스템에 사용되는 기능을 제공하는 클래스.
 */
@UtilityClass
public final class GameUtil {
    /** 게임 맵 목록 (게임 모드 : 게임 맵 목록) */
    private final EnumMap<GamePlayMode, GameMap[]> gameMapListMap = new EnumMap<>(GamePlayMode.class);
    /** 게임 모드 목록 (랭크 여부 : 게임 모드 목록) */
    private final HashMap<Boolean, GamePlayMode[]> gamePlayModes = new HashMap<>();

    static {
        gameMapListMap.put(GamePlayMode.TEAM_DEATHMATCH, TeamDeathmatchMap.values());
        gamePlayModes.put(false, Arrays.stream(GamePlayMode.values())
                .filter(gamePlayMode -> !gamePlayMode.isRanked()).toArray(GamePlayMode[]::new));
        gamePlayModes.put(true, Arrays.stream(GamePlayMode.values())
                .filter(GamePlayMode::isRanked).toArray(GamePlayMode[]::new));
    }

    /**
     * 지정한 게임 모드에 해당하는 무작위 맵을 반환한다.
     *
     * @param gamePlayMode 게임 모드
     * @return 무작위 맵
     */
    @NonNull
    public GameMap getRandomMap(@NonNull GamePlayMode gamePlayMode) {
        int index = DMGR.getRandom().nextInt(gameMapListMap.get(gamePlayMode).length);
        return gameMapListMap.get(gamePlayMode)[index];
    }

    /**
     * 일반 또는 랭크 게임의 무작위 게임 모드를 반환한다.
     *
     * @param isRanked 랭크 여부
     * @return 무작위 게임 모드
     */
    @NonNull
    public GamePlayMode getRandomGamePlayMode(boolean isRanked) {
        int index = DMGR.getRandom().nextInt(gamePlayModes.get(isRanked).length);
        return gamePlayModes.get(isRanked)[index];
    }
}
