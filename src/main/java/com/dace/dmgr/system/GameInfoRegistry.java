package com.dace.dmgr.system;

import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameMode;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.game.map.TeamDeathmatchMap;

import java.util.EnumMap;
import java.util.Random;

/**
 * 게임 관련 데이터를 저장하고 관리하는 클래스.
 */
public final class GameInfoRegistry {
    /** 게임 모드별 최대 방 갯수 */
    private static final int MAX_ROOM = 3;

    private static final EnumMap<GameMode, Game[]> gameListMap = new EnumMap<>(GameMode.class);
    private static final EnumMap<GameMode, GameMap[]> gameMapListMap = new EnumMap<>(GameMode.class);
    private static final Random random = new Random();

    /**
     * 게임 모드별 맵을 저장한다.
     */
    public static void init() {
        gameMapListMap.put(GameMode.TEAM_DEATHMATCH, TeamDeathmatchMap.values());
    }

    /**
     * 지정한 게임 모드에 해당하는 무작위 맵을 반환한다.
     *
     * @param gameMode 게임 모드
     * @return 무작위 맵
     */
    public static GameMap getRandomMap(GameMode gameMode) {
        int index = random.nextInt(gameMapListMap.get(gameMode).length);
        return gameMapListMap.get(gameMode)[index];
    }

    /**
     * @param gameMode 게임 모드
     * @param number   방 번호
     * @return 게임 정보 객체
     */
    public static Game getGame(GameMode gameMode, int number) {
        gameListMap.putIfAbsent(gameMode, new Game[MAX_ROOM]);
        return gameListMap.get(gameMode)[number];
    }

    /**
     * @param number 방 번호
     * @param game   게임 정보 객체
     */
    public static void addGame(int number, Game game) {
        gameListMap.putIfAbsent(game.getGameMode(), new Game[MAX_ROOM]);
        gameListMap.get(game.getGameMode())[number] = game;
    }

    /**
     * @param gameMode 게임 모드
     * @param number   방 번호
     */
    public static void removeGame(GameMode gameMode, int number) {
        gameListMap.putIfAbsent(gameMode, new Game[MAX_ROOM]);
        gameListMap.get(gameMode)[number] = null;
    }
}
