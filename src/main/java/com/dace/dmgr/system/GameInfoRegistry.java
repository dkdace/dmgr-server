package com.dace.dmgr.system;

import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameConfig;
import com.dace.dmgr.game.GamePlayMode;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.game.map.TeamDeathmatchMap;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Random;

/**
 * 게임 관련 데이터를 저장하고 관리하는 클래스.
 */
public final class GameInfoRegistry {
    /** 게임 목록 (랭크 여부 : 게임 목록) */
    private static final HashMap<Boolean, Game[]> gameListMap = new HashMap<>();
    /** 게임 맵 목록 (게임 모드 : 게임 맵) */
    private static final EnumMap<GamePlayMode, GameMap[]> gameMapListMap = new EnumMap<>(GamePlayMode.class);
    /** 게임 모드 목록 (랭크 여부 : 게임 모드 목록) */
    private static final HashMap<Boolean, GamePlayMode[]> gamePlayModes = new HashMap<>();
    private static final Random random = new Random();

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
    public static GameMap getRandomMap(GamePlayMode gamePlayMode) {
        int index = random.nextInt(gameMapListMap.get(gamePlayMode).length);
        return gameMapListMap.get(gamePlayMode)[index];
    }

    /**
     * 일반 또는 랭크 게임의 무작위 게임 모드를 반환한다.
     *
     * @param isRanked 랭크 여부
     * @return 무작위 게임 모드
     */
    public static GamePlayMode getRandomGamePlayMode(boolean isRanked) {
        int index = random.nextInt(gamePlayModes.get(isRanked).length);
        return gamePlayModes.get(isRanked)[index];
    }

    /**
     * 일반 또는 랭크 게임의 게임 모드 목록을 반환한다.
     *
     * @param isRanked 랭크 여부
     * @return 게임 모드 목록
     */
    public static GamePlayMode[] getGamePlayModes(boolean isRanked) {
        return gamePlayModes.get(isRanked);
    }

    /**
     * @param isRanked 랭크 여부
     * @param number   방 번호
     * @return 게임 정보 객체
     */
    public static Game getGame(boolean isRanked, int number) {
        gameListMap.putIfAbsent(isRanked, new Game[GameConfig.MAX_ROOM_COUNT]);
        return gameListMap.get(isRanked)[number];
    }

    /**
     * @param game 게임 정보 객체
     */
    public static void addGame(Game game) {
        gameListMap.putIfAbsent(game.getGamePlayMode().isRanked(), new Game[GameConfig.MAX_ROOM_COUNT]);
        gameListMap.get(game.getGamePlayMode().isRanked())[game.getNumber()] = game;
    }

    /**
     * @param game 게임 정보 객체
     */
    public static void removeGame(Game game) {
        gameListMap.putIfAbsent(game.getGamePlayMode().isRanked(), new Game[GameConfig.MAX_ROOM_COUNT]);
        gameListMap.get(game.getGamePlayMode().isRanked())[game.getNumber()] = null;
    }
}
