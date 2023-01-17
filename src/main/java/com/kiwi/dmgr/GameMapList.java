package com.kiwi.dmgr;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 각 모드마다 게임의 리스트를 보관하는 클래스
 */
public class GameMapList {

    public static final HashMap<GameType, ArrayList<Game>> gameMap = new HashMap<>();
    public static final HashMap<Player, GameUser> gameUserMap = new HashMap<>();

    /**
     * 게임 리스트에 게임을 쉽게 추가하도록 하는 함수
     *
     * @param game 게임
     */
    public static void gameAdd(Game game) {
        gameMap.get(game.getType()).add(game);
    }
}
