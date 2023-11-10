package com.dace.dmgr.system;

import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GamePlayMode;
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

    /** 게임 목록 (게임 모드 : 게임) */
    private static final EnumMap<GamePlayMode, Game[]> gameListMap = new EnumMap<>(GamePlayMode.class);
    /** 게임 맵 목록 (게임 모드 : 게임 맵) */
    private static final EnumMap<GamePlayMode, GameMap[]> gameMapListMap = new EnumMap<>(GamePlayMode.class);
    private static final Random random = new Random();

    /**
     * 게임 모드별 맵을 저장한다.
     */
    private static void init() {
        gameMapListMap.put(GamePlayMode.TEAM_DEATHMATCH, TeamDeathmatchMap.values());
    }

    /**
     * 지정한 게임 모드에 해당하는 무작위 맵을 반환한다.
     *
     * @param gamePlayMode 게임 모드
     * @return 무작위 맵
     */
    public static GameMap getRandomMap(GamePlayMode gamePlayMode) {
        if (gameMapListMap.isEmpty())
            init();

        int index = random.nextInt(gameMapListMap.get(gamePlayMode).length);
        return gameMapListMap.get(gamePlayMode)[index];
    }

    /**
     * @param gamePlayMode 게임 모드
     * @param number       방 번호
     * @return 게임 정보 객체
     */
    public static Game getGame(GamePlayMode gamePlayMode, int number) {
        gameListMap.putIfAbsent(gamePlayMode, new Game[MAX_ROOM]);
        return gameListMap.get(gamePlayMode)[number];
    }

    /**
     * @param game 게임 정보 객체
     */
    public static void addGame(Game game) {
        gameListMap.putIfAbsent(game.getGamePlayMode(), new Game[MAX_ROOM]);
        gameListMap.get(game.getGamePlayMode())[game.getNumber()] = game;
    }

    /**
     * @param game 게임 정보 객체
     */
    public static void removeGame(Game game) {
        gameListMap.putIfAbsent(game.getGamePlayMode(), new Game[MAX_ROOM]);
        gameListMap.get(game.getGamePlayMode())[game.getNumber()] = null;
    }
}
