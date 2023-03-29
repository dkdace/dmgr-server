package com.kiwi.dmgr.game;

import com.kiwi.dmgr.game.map.GameMap;
import com.kiwi.dmgr.game.mode.GameMode;
import com.kiwi.dmgr.match.MatchType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 각 모드마다 게임의 리스트를 보관하는 클래스
 */
public class GameMapList {

    public static final HashMap<MatchType, ArrayList<Game>> gameList = new HashMap<>();
    public static final HashMap<MatchType, ArrayList<GameMode>> gameMatchModeList = new HashMap<>();
    public static final HashMap<Player, GameUser> gameUserMap = new HashMap<>();
    public static final HashMap<GameMode, ArrayList<GameMap>> gameMapList = new HashMap<>();

    /**
     * 게임 리스트에 게임을 쉽게 추가하도록 하는 함수
     *
     * @param game 게임
     */
    public static void addGame(Game game) {
        gameList.get(game.getMatchType()).add(game);
    }

    /**
     * 게임 리스트에 게임을 쉽게 제거하도록 하는 함수
     *
     * @param game 게임
     */
    public static void delGame(Game game) {
        gameList.get(game.getMatchType()).remove(game);
    }

    /**
     * 게임 모드 리스트에 모드를 쉽게 추가하도록 하는 함수
     *
     * @param matchType 매치 타입
     * @param gameMode 게임 모드
     */
    public static void addMatchMode(MatchType matchType, GameMode gameMode) {
        gameMatchModeList.get(matchType).add(gameMode);
    }

    /**
     * 게임 모드 리스트에 모드를 쉽게 제거하도록 하는 함수
     *
     * @param matchType 매치 타입
     * @param gameMode 게임 모드
     */
    public static void delMatchMode(MatchType matchType, GameMode gameMode) {
        gameMatchModeList.get(matchType).remove(gameMode);
    }
}
