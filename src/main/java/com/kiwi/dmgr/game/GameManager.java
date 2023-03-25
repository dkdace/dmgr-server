package com.kiwi.dmgr.game;

import com.dace.dmgr.lobby.User;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.dace.dmgr.system.HashMapList.userMap;
import static com.kiwi.dmgr.game.GameMapList.gameList;

/**
 * 게임을 관리하는 클래스
 * 모든 매치 타입에 사용할 수 있다.
 */
public class GameManager extends Game {

    /**
     * 플레이어를 MMR 기준으로 내림차순 정렬한다.
     *
     * @param playerList 플레이어 목록
     * @return 정렬된 플레이어 목록
     */
    public static ArrayList<Player> getPlayerListMMRSort(ArrayList<Player> playerList) {
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
    public static HashMap<Team, ArrayList<Player>> getPlayerListDivision(Game game) {
        HashMap<Team, ArrayList<Player>> teamPlayerMapList = new HashMap<>();
        ArrayList<Player> team1 = new ArrayList<>();
        ArrayList<Player> team2 = new ArrayList<>();
        ArrayList<Player> playerList = getPlayerListMMRSort(game.getPlayerList());

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
