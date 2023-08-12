package com.kiwi.dmgr.game;

import com.kiwi.dmgr.game.map.GameMap;
import com.kiwi.dmgr.game.mode.EnumGameMode;
import com.kiwi.dmgr.game.mode.GameMode;
import com.kiwi.dmgr.match.MatchType;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 각 모드마다 게임의 리스트를 보관하는 클래스
 */
public class GameMapList {

    /* 매치 타입마다 게임을 보관하는 리스트 */
    public static HashMap<MatchType, ArrayList<Game>> gameList = new HashMap<>();
    /* 매치 타입마다 플레이 가능한 게임모드를 보관하는 리스트 */
    public static HashMap<MatchType, ArrayList<GameMode>> gameMatchModeList = new HashMap<>();
    /* 현재 플레이어가 최근 게임 중인 해당 게임의 게임유저를 저장하는 맵 */
    public static HashMap<Player, GameUser> gameUserMap = new HashMap<>();
    /* 게임 모드마다 플레이 가능한 게임 맵을 보관하는 리스트 */
    public static HashMap<EnumGameMode, ArrayList<GameMap>> gameMapList = new HashMap<>();

    /**
     * 게임 리스트에 게임을 쉽게 추가하도록 하는 함수
     *
     * @param game 게임
     */
    public static void addGame(Game game) {
        if (gameList.get(game.getMatchType()) == null)
            gameList.put(game.getMatchType(), new ArrayList<>(Collections.singletonList(game)));
        else
            gameList.get(game.getMatchType()).add(game);
    }

    /**
     * 게임 리스트에 게임을 쉽게 제거하도록 하는 함수
     *
     * @param game 게임
     */
    public static void delGame(Game game) {
        if (gameList.get(game.getMatchType()) != null)
            gameList.get(game.getMatchType()).remove(game);
    }

    /**
     * 게임이 유효한지 출력하는 함수
     *
     * @param game 게임
     * @return 유효 여부
     */
    public static boolean vaildGame(Game game) {
        return gameList.get(game.getMatchType()).contains(game);
    }

    /**
     * 게임 모드 리스트에 모드를 쉽게 추가하도록 하는 함수
     *
     * @param matchType 매치 타입
     * @param gameMode 게임 모드
     */
    public static void addMatchMode(MatchType matchType, GameMode gameMode) {
        if (gameMatchModeList.get(matchType) == null)
            gameMatchModeList.put(matchType, new ArrayList<>(Collections.singletonList(gameMode)));
        else
            gameMatchModeList.get(matchType).add(gameMode);
    }

    /**
     * 게임 모드 리스트에 모드를 쉽게 제거하도록 하는 함수
     *
     * @param matchType 매치 타입
     * @param gameMode 게임 모드
     */
    public static void delMatchMode(MatchType matchType, GameMode gameMode) {
        if (gameMatchModeList.get(matchType) != null)
            gameMatchModeList.get(matchType).remove(gameMode);
    }

    /**
     * 게임 맵 리스트에 맵을 쉽게 추가하도록 하는 함수
     *
     * @param map 맵
     */
    public static void addMap(GameMap map) {
        if (gameMapList.get(map.getMode()) == null)
            gameMapList.put(map.getMode(), new ArrayList<>(Collections.singletonList(map)));
        else
            gameMapList.get(map.getMode()).add(map);
    }

    /**
     * 게임 맵 리스트에서 특정 모드에서 플레이 가능한 맵 중 하나를 출력하는 함수
     *
     * @param mode 게임 모드
     * @return 맵
     */
    public static GameMap getRandomMap(EnumGameMode mode) {
        ArrayList<GameMap> mapList;
        mapList = gameMapList.get(mode);

        return mapList.get(new Random().nextInt(mapList.size()));
    }
}
