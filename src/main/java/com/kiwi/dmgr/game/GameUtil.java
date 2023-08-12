package com.kiwi.dmgr.game;

import org.bukkit.entity.Player;

import static com.kiwi.dmgr.game.GameMapList.gameUserMap;

/**
 * 게임 시스템에 관련된 기능을 제공하는 클래스.
 */
public class GameUtil {

    /**
     * 두 유저가 게임 유저 이벤트에 유효한지의(사용 가능한지의) 여부를 출력한다.
     *
     * <p> 게임 유저 데이터가 존재하며, 한 게임에 있으면 true를 출력한다. </p>
     *
     * @param user1 유저1
     * @param user2 유저2
     * @return 이벤트 유효 여부
     */
    public static boolean isGameUserEventVaild(GameUser user1, GameUser user2) {
        return user1.getGame() == user2.getGame();
    }

    /**
     * 해당 플레이어가 탈주 플레이어인지 여부를 출력한다.
     *
     * @param player 플레이어
     * @return 탈주 플레이어 여부
     */
    public static boolean isPlayerEscaped(Player player) {
        return gameUserMap.containsKey(player);
    }
}
