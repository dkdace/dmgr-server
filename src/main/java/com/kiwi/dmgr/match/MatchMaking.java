package com.kiwi.dmgr.match;

import com.kiwi.dmgr.game.Game;
import com.kiwi.dmgr.game.mode.GameMode;
import org.bukkit.entity.Player;

import static com.kiwi.dmgr.game.GameMapList.gameList;


/**
 * 게임 대기열 클래스
 */
public class MatchMaking {

    private final int WAIT_TIME = 60;

    /**
     * 일반전 매치메이킹 대기열에 플레이어를 추가한다.
     * 빠른 입장을 통해 추가된 경우이다.
     *
     * 처리는 아래의 우선 순위를 거친다.
     * 1. 난입 유저가 필요한 게임이 있을 경우 해당 게임에 플레이어 난입
     * 2. 게임이 시작되지 않고 대기중이면 해당 게임에 플레이어 추가
     * 3. 게임 인스턴스 호출 후 해당 게임에 플레이어 추가
     *
     * @param player 플레이어
     * @param mode 게임모드
     */
    public static void addPlayerUnranked(Player player, GameMode mode) {
        for (Game game : gameList.get(MatchType.UNRANKED)) {
            if (game.isNeedPlayer()) {
                game.joinPlayer(player);
                return;
            }
        }

        for (Game game : gameList.get(MatchType.UNRANKED)) {
            if (!game.isPlay()) {
                game.joinPlayer(player);
                return;
            }
        }

        Game newGame = new Game(MatchType.UNRANKED, mode);
        newGame.joinPlayer(player);
    }

    /**
     * 일반전 매치메이킹 대기열에 플레이어를 추가한다.
     * 빠른 입장을 통해 추가된 경우이다.
     *
     * 처리는 아래의 우선 순위를 거친다.
     * 1. 난입 유저가 필요한 게임이 있을 경우 해당 게임에 플레이어 난입
     * 2. 게임이 시작되지 않고 대기중이면 해당 게임에 플레이어 추가
     *
     * 3. 1, 2번에 해당하는 게임이 없을 경우, 일정 시간 대기.
     *    만약 2명 이상의 대기자가 발생한다면 최대 플레이어수에
     *    도달하지 못한 게임에 2명씩 추가
     *
     * 4. 게임 인스턴스 호출 후 해당 게임에 플레이어 추가
     *
     * @param player 플레이어
     * @param type 게임모드

    public static void addPlayer(Player player, MatchType type) {
        for (Game game : gameList.get(type)) {
            if (game.isNeedPlayer()) {
                // 난입
                return;
            }
        }

        for (Game game : gameList.get(type)) {
            if (!game.isPlay()) {
                // 참가
                return;
            }
        }

        Game newGame = new Game(type);
        // 참가
    }
    */

}
