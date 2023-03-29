package com.kiwi.dmgr.game;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.lobby.User;
import com.kiwi.dmgr.game.map.GameMap;
import com.kiwi.dmgr.game.mode.GameMode;
import com.kiwi.dmgr.match.MatchType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

import static com.dace.dmgr.system.HashMapList.userMap;

/**
 * 게임의 정보를 담는 클래스
 */
@Getter
@Setter
public class Game {

    /** 플레이어 목록 */
    private ArrayList<Player> playerList;
    /** 대기중인 플레이어 목록 */
    private ArrayList<Player> waitPlayerList;
    /** 플레이어 팀 */
    private HashMap<Team, ArrayList<Player>> teamPlayerMapList;
    /** 팀 스코어 */
    private HashMap<Team, Integer> teamScore;
    /** 잔여 시간 */
    private long remainTime;
    /** 게임 모드 */
    private GameMode mode;
    /** 게임 매치 타입 */
    private MatchType matchType;
    /** 게임 진행 여부 */
    private boolean play;
    /** 전장 월드 */
    private World world;
    /** 맵 */
    private GameMap map;

    /**
     * 게임 인스턴스를 호출하고 {@link GameMapList#gameList}에 추가한다.
     */
    public Game() {
        this.playerList = new ArrayList<>();
        this.waitPlayerList = new ArrayList<>();
        this.teamPlayerMapList = new HashMap<>();
        this.teamScore = new HashMap<>();
        this.remainTime = 0;
        this.matchType = null;
        this.mode = null;
        this.play = false;
        this.world = null;
    }

    public Game(MatchType type, GameMode mode) {
        super();
        this.playerList = null;
        this.matchType = type;
        this.mode = mode;

        GameMapList.addGame(this);
        GameScheduler.run(this);
    }

    /**
     * 게임 인스턴스를 제거한다.
     */
    public void delete() {
        GameMapList.delGame(this);
    }

    /**
     * 해당 게임에 플레이어 입장을 원활하게 하기 위해 초기 상태로 설정한다.
     *
     * @param player 플레이어
     */
    public void initPlayer(Player player) {
        GameUser user = new GameUser(player);
        user.setGame(this);
        if (!playerList.contains(player))
            playerList.add(player);
    }

    public void initPlayer(Player player, Team team) {
        initPlayer(player);

        for (Team tempTeam : Team.values())
            teamPlayerMapList.get(team).remove(player);

        teamPlayerMapList.get(team).add(player);
        player.teleport(map.getTeamSpawnLocation().get(team));
    }

    /**
     * 게임에 플레이어를 추가한다.
     *
     * 게임이 시작되지 않았으면 해당 월드의 대기방으로 이동
     * 게임 중일 경우(난입) 난입이 필요한 팀으로 이동
     * 난입이 필요한 팀이 없으면 대기 플레이어 리스트에 추가
     * 대기 플레이어 리스트에 2명이 있으면 MMR에 맞게 각 팀에 난입
     *
     * @param player 플레이어
     */
    public void joinPlayer(Player player) {
        if (!play) {
            initPlayer(player);
        }

        else {
            int redAmount = teamPlayerMapList.get(Team.RED).size();
            int blueAmount = teamPlayerMapList.get(Team.BLUE).size();
            Team team = null;

            if (redAmount < blueAmount)
                team = Team.RED;

            else if (redAmount > blueAmount)
                team = Team.BLUE;

            else {
                if (waitPlayerList.size() == 1) {
                    int user1MMR = userMap.get(waitPlayerList.get(0)).getMMR();
                    int user2MMR = userMap.get(player).getMMR();

                    Player player1 = user1MMR >= user2MMR ? waitPlayerList.get(0) : player;
                    Player player2 = user1MMR < user2MMR ? waitPlayerList.get(0) : player;

                    int redMMR = 0;
                    int blueMMR = 0;

                    for (Player tempPlayer : teamPlayerMapList.get(Team.RED)) {
                        User tempUser = userMap.get(tempPlayer);
                        redMMR += tempUser.getMMR();
                    }

                    for (Player tempPlayer : teamPlayerMapList.get(Team.BLUE)) {
                        User tempUser = userMap.get(tempPlayer);
                        blueMMR += tempUser.getMMR();
                    }

                    if (redMMR < blueMMR) {
                        initPlayer(player1, Team.RED);
                        initPlayer(player2, Team.BLUE);
                    } else {
                        initPlayer(player1, Team.BLUE);
                        initPlayer(player2, Team.RED);
                    }
                    waitPlayerList.remove(0);
                }

                else
                    waitPlayerList.add(player);
            }

        }
    }

    /**
     * 게임에 있는 모든 플레이어에게 알림 메세지를 보낸다.
     *
     * @param message 메세지
     */
    protected void sendAlertMessage(String message) {
        for (Player player : playerList)
            player.sendMessage(DMGR.PREFIX.CHAT + message);
    }

    /**
     * 게임에 난입 인원이 필요한지 여부를 리턴한다.
     * 인원이 홀수이면 true 를 반환한다.
     */
    public boolean isNeedPlayer() {
        return (playerList.size() % 2 != 0);
    }
}
