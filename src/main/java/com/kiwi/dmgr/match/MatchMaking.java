package com.kiwi.dmgr.match;

import com.dace.dmgr.lobby.User;
import com.kiwi.dmgr.Game;
import com.kiwi.dmgr.GameType;
import com.kiwi.dmgr.Team;
import org.bukkit.entity.Player;

import java.util.*;

import static com.dace.dmgr.system.HashMapList.userMap;
import static com.kiwi.dmgr.GameMapList.gameMap;

/**
 * 게임 대기열 클래스
 */
public class MatchMaking {

    /**
     * 게임에 플레이어를 추가한다.
     *
     * 처리는 아래의 우선 순위를 거친다.
     * 1. 난입 유저가 필요한 게임이 있을 경우 해당 게임에 플레이어 난입
     * 2. 게임이 시작되지 않고 대기중이면 해당 게임에 플레이어 추가
     * 3. 게임 인스턴스 호출 후 해당 게임에 플레이어 추가
     *
     * @param player 플레이어
     * @param type 게임모드
     */
    public static void addPlayer(Player player, GameType type) {
        for (Game game : gameMap.get(type)) {
            if (game.isNeedPlayer()) {
                // 난입
                return;
            }
        }

        for (Game game : gameMap.get(type)) {
            if (!game.isPlay()) {
                // 참가
                return;
            }
        }

        Game newGame = new Game(type);
        // 참가
    }

    /**
     * 플레이어를 MMR 기준으로 내림차순 정렬한다.
     *
     * @param playerList 플레이어 목록
     * @return 정렬된 플레이어 목록
     */
    public static ArrayList<Player> playerMMRSort(ArrayList<Player> playerList) {
        HashMap<Player, Integer> playerMap = new HashMap<>();
        for (Player player : playerList) {
            User user = userMap.get(player);
            playerMap.put(player, user.getMMR());
        }

        List<Player> keySetList = new ArrayList<>(playerMap.keySet());
        keySetList.sort((o1, o2) -> (playerMap.get(o1).compareTo(playerMap.get(o2))));

        return new ArrayList<>(keySetList);
    }

    /**
     * MMR에 따라 팀 분배를 하여 플레이어를 실력에 따라 나눈다.
     *
     * 팀 분배는 아래와 같이 이뤄진다. (플레이어 수를 i라고 하자.)
     * 먼저 최상위 플레이어를 일부 얻는다. (i / 4의 내림값의 인원수)
     * 그 최상위 플레이어를 1팀으로 이동시킨다.
     * 그리고 중위권 플레이어를 2팀에 전부 이동시킨다.
     * 나머지 하위권 플레이어를 1팀으로 전부 이동시킨다.
     * 1팀, 2팀을 무작위로 RED, BLUE 팀으로 선정한다.
     *
     * @param game 게임
     * @return 분배된 팀 맵리스트
     */
    public static HashMap<Team, ArrayList<Player>> playerDivision(Game game) {
        HashMap<Team, ArrayList<Player>> teamPlayerMapList = new HashMap<>();
        ArrayList<Player> team1 = new ArrayList<>();
        ArrayList<Player> team2 = new ArrayList<>();
        ArrayList<Player> playerList = playerMMRSort(game.getPlayerList());

        int size = game.getPlayerList().size();
        for (int i=0; i<size/4; i++) {
            team1.add(playerList.get(0));
            playerList.remove(0);
        }

        for (int i=0; i<size/2; i++) {
            team2.add(playerList.get(0));
            playerList.remove(0);
        }

        team1.addAll(playerList);

        Random random = new Random();
        double r = random.nextDouble();

        if (r < 0.5) {
            teamPlayerMapList.put(Team.RED, team1);
            teamPlayerMapList.put(Team.BLUE, team2);
        } else {
            teamPlayerMapList.put(Team.BLUE, team1);
            teamPlayerMapList.put(Team.RED, team2);
        }
        
        return teamPlayerMapList;
    }
}
