package com.kiwi.dmgr.game;

import com.dace.dmgr.lobby.Lobby;
import com.dace.dmgr.lobby.User;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.SystemPrefix;
import com.dace.dmgr.util.SoundUtil;
import com.kiwi.dmgr.game.map.GameMap;
import com.kiwi.dmgr.game.map.Point;
import com.kiwi.dmgr.game.map.WorldManager;
import com.kiwi.dmgr.game.mode.EnumGameMode;
import com.kiwi.dmgr.game.mode.GameMode;
import com.kiwi.dmgr.match.MatchType;
import com.kiwi.dmgr.match.RankRating;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 게임의 정보를 담고 관리하는 클래스
 */
@Getter
@Setter
public class Game {
    /** 플레이어 목록 */
    private ArrayList<Player> playerList;
    /** 게임 유저 맵 */
    private HashMap<Player, GameUser> gameUserMap;
    /** 대기중인 플레이어 목록 */
    private ArrayList<Player> waitPlayerList;
    /** 플레이어 팀 */
    private HashMap<Team, ArrayList<Player>> teamPlayerMapList;
    /** 팀 스코어 */
    private HashMap<Team, Integer> teamScore;
    /** 잔여 시간 */
    private int remainTime;
    /** 플레이 시간 */
    private int playTime;
    /** 게임 모드 */
    private EnumGameMode mode;
    /** 게임 매치 타입 */
    private MatchType matchType;
    /** 게임 진행 여부 */
    private boolean play;
    /** 전장 월드 */
    private String world;
    /** 맵 */
    private GameMap map;

    /**
     * 게임 인스턴스를 호출하고 {@link GameMapList#gameList}에 추가한다.
     */
    public Game(MatchType type, EnumGameMode mode) {
        this.playerList = new ArrayList<>();
        this.gameUserMap = new HashMap<>();
        this.waitPlayerList = new ArrayList<>();
        this.teamPlayerMapList = new HashMap<>();
        this.teamScore = new HashMap<>();
        this.remainTime = 0;
        this.playTime = 0;
        this.play = false;
        this.world = null;
        this.matchType = type;
        this.mode = mode;
        this.map = GameMapList.getRandomMap(this.mode);
        this.world = map.getWorldName() + "_" + UUID.randomUUID();

        for (Team tempTeam : Team.values())
            this.teamPlayerMapList.put(tempTeam, new ArrayList<>());

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
     * 월드를 로드한다.
     */
    public void loadWorld() {
        WorldManager.generateWorld(map.getWorldName(), world);
    }

    /**
     * 월드를 언로드한다.
     */
    public void unloadWorld() {
        WorldManager.unloadWorld(world);
    }

    /**
     * 게임을 시작할 때 이 함수를 호출해야한다.
     *
     * @see GameMode
     */
    public void start() {
        this.play = true;
        this.remainTime = this.mode.getPlayTime();

        for (Team team : Team.values())
            this.teamScore.put(team, 0);
    }

    /**
     * 게임이 끝날 때 이 함수를 호출해야한다.
     * 강제종료로 끝나는 경우 매치 결과, 보상 및 랭크 변동이 스킵된다.
     *
     * @param force 강제종료 여부
     * @see GameMode
     */
    public void finish(boolean force) {
        this.play = false;

        for (int i = 0; i < 20; i++)
            this.sendAlertMessage("");

        Team winner = teamScore.get(Team.RED).equals(teamScore.get(Team.BLUE)) ? Team.NONE :
                teamScore.get(Team.RED) > teamScore.get(Team.BLUE) ? Team.RED : Team.BLUE;

        if (!force) {
            if (this.matchType == MatchType.COMPETITIVE) {
                for (Player player : this.playerList) {
                    RankRating.updateRankAfterGame(player, winner);
                }
            } else if (this.matchType == MatchType.UNRANKED) {
                for (Player player : this.playerList) {
                    RankRating.updateMMRAfterGame(player);
                }
            }

            this.sendAlertMessage(this.mode.getName() + " " + this.matchType.getName() + "전 게임 결과");

            if (winner == Team.NONE)
                this.sendAlertMessage("* " + winner.color + "무승부");
            else
                this.sendAlertMessage("* " + winner.color + winner.name() + "팀 승리");
            this.sendAlertMessage("");

            this.sendAlertMessage("내 게임 성적");
            for (Player player : this.playerList) {
                GameUser gameUser = gameUserMap.get(player);
                this.sendAlertMessage(player, "* 점수: " + gameUser.getScore());
                this.sendAlertMessage(player, "* K/D/A: " + gameUser.getKill() + "/" + gameUser.getDeath() + "/" + gameUser.getAssist() +
                        " (" + (gameUser.getKDA()) + ")");
                this.sendAlertMessage(player, "* 입힌 데미지: " + gameUser.getOutgoingDamage());
            }
            this.sendAlertMessage("");
        } else {
            this.sendAlertMessage("관리자에 의해 게임이 강제종료 됨");
        }

        for (Player player : this.playerList) {
            GameMapList.gameUserMap.remove(player);
            player.teleport(Lobby.lobbyLocation);
            SoundUtil.play(Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F, player);
        }
        this.unloadWorld();
        this.delete();
    }

    /**
     * 플레이어를 MMR 기준으로 내림차순 정렬한다.
     *
     * @param playerList 플레이어 목록
     * @return 정렬된 플레이어 목록
     */
    private ArrayList<Player> getPlayerListMMRSort(ArrayList<Player> playerList) {
        HashMap<Player, Integer> playerMap = new HashMap<>();
        for (Player player : playerList) {
            User user = EntityInfoRegistry.getUser(player);
            playerMap.put(player, user.getMMR());
        }

        List<Player> keySetList = new ArrayList<>(playerMap.keySet());
        keySetList.sort(Comparator.comparing(playerMap::get));

        return new ArrayList<>(keySetList);
    }

    /**
     * MMR에 따라 팀 분배를 하여 플레이어를 실력에 따라 나눈다.
     *
     * <p> 팀 분배는 아래와 같이 이뤄진다. (플레이어 수를 i라고 하자.)
     * 먼저 최상위 플레이어를 일부 얻는다. (i / 4의 내림값의 인원수)
     * 그 최상위 플레이어를 1팀으로 이동시킨다.
     * 그리고 중위권 플레이어를 2팀에 전부 이동시킨다.
     * 나머지 하위권 플레이어를 1팀으로 전부 이동시킨다.
     * 1팀, 2팀을 무작위로 RED, BLUE 팀으로 선정한다. </p>
     */
    public void teamDivide() {
        playerList = getPlayerListMMRSort(this.playerList);
        ArrayList<Player> team1 = new ArrayList<>();
        ArrayList<Player> team2 = new ArrayList<>();

        int size = this.playerList.size();
        for (int i = 0; i < size / 4; i++) {
            team1.add(playerList.get(0));
            playerList.remove(0);
        }

        for (int i = 0; i < size / 2; i++) {
            team2.add(playerList.get(0));
            playerList.remove(0);
        }

        team1.addAll(playerList);

        Random random = new Random();
        double r = random.nextDouble();

        if (r < 0.5) {
            teamPlayerMapList.put(Team.RED, team1);
            teamPlayerMapList.put(Team.BLUE, team2);
            for (Player player : team1)
                initPlayer(player, Team.RED);
            for (Player player : team2)
                initPlayer(player, Team.BLUE);
        } else {
            teamPlayerMapList.put(Team.BLUE, team1);
            teamPlayerMapList.put(Team.RED, team2);
            for (Player player : team1)
                initPlayer(player, Team.BLUE);
            for (Player player : team2)
                initPlayer(player, Team.RED);
        }
    }

    /**
     * 해당 게임의 평균 통계 지표를 반환한다.
     *
     * @param fieldName 지표
     * @return 평균 지표
     */
    public double getAverageIndicator(String fieldName) {
        return playerList.stream()
                .mapToDouble(player -> {
                    User user = EntityInfoRegistry.getUser(player);
                    GameUser gameUser = gameUserMap.get(player);
                    switch (fieldName) {
                        case "MMR":
                            return user.getMMR();
                        case "Rank":
                            return user.getRank();
                        case "score":
                            return gameUser.getScore();
                        case "kill":
                            return gameUser.getKill();
                        case "death":
                            return gameUser.getDeath();
                        case "assist":
                            return gameUser.getAssist();
                        default:
                            throw new IllegalArgumentException("해당 지표는 존재하지 않습니다.");
                    }
                })
                .average()
                .orElseThrow(() -> new IllegalArgumentException("플레이어가 존재하지 않습니다."));
    }

    /**
     * 해당 게임에 플레이어 입장을 원활하게 하기 위해 초기 상태로 설정한다.
     *
     * @param player 플레이어
     */
    public void initPlayer(Player player) {
        GameUser user = new GameUser(player, this);
        this.gameUserMap.put(player, user);
        if (!playerList.contains(player))
            playerList.add(player);
    }

    public void initPlayer(Player player, Team team) {
        initPlayer(player);
        GameUser user = gameUserMap.get(player);
        user.setTeam(team);
        sendAlertMessage(player.getName() + " - " + team.name());

        for (Team tempTeam : Team.values())
            teamPlayerMapList.get(tempTeam).remove(player);

        teamPlayerMapList.get(team).add(player);

        player.teleport(getPointLocation(team));
    }

    /**
     * 게임에 플레이어를 추가한다.
     *
     * <p> 게임이 시작되지 않았으면 해당 월드의 대기방으로 이동
     * 게임 중일 경우(난입) 난입이 필요한 팀으로 이동
     * 난입이 필요한 팀이 없으면 대기 플레이어 리스트에 추가
     * 대기 플레이어 리스트에 2명이 있으면 MMR에 맞게 각 팀에 난입 </p>
     *
     * @param player 플레이어
     */
    public void joinPlayer(Player player) {
        if (!play) {
            initPlayer(player);
            sendAlertMessage(player.getName() + "게임 추가");
        } else {
            int redAmount = teamPlayerMapList.get(Team.RED).size();
            int blueAmount = teamPlayerMapList.get(Team.BLUE).size();

            if (redAmount < blueAmount)
                initPlayer(player, Team.RED);

            else if (redAmount > blueAmount)
                initPlayer(player, Team.BLUE);

            else {
                if (waitPlayerList.size() == 1) {
                    int user1MMR = EntityInfoRegistry.getUser(waitPlayerList.get(0)).getMMR();
                    int user2MMR = EntityInfoRegistry.getUser(player).getMMR();

                    Player player1 = user1MMR >= user2MMR ? waitPlayerList.get(0) : player;
                    Player player2 = user1MMR < user2MMR ? waitPlayerList.get(0) : player;

                    int redMMR = 0;
                    int blueMMR = 0;

                    for (Player tempPlayer : teamPlayerMapList.get(Team.RED)) {
                        User tempUser = EntityInfoRegistry.getUser(tempPlayer);
                        redMMR += tempUser.getMMR();
                    }

                    for (Player tempPlayer : teamPlayerMapList.get(Team.BLUE)) {
                        User tempUser = EntityInfoRegistry.getUser(tempPlayer);
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
                } else
                    waitPlayerList.add(player);
            }

        }
    }

    /**
     * 게임 월드의 포인트 위치를 리턴한다.
     *
     * @param point 주요 위치 이름
     * @see Point
     */
    public Location getPointLocation(Point point) {
        World world = Bukkit.getWorld(this.world);
        double[] args = this.map.getPointLocation().get(point);
        return new Location(world, args[0], args[1], args[2], (float) args[3], (float) args[4]);
    }

    public Location getPointLocation(Team team) {
        if (team == Team.RED) return getPointLocation(Point.RED_SPAWN);
        else if (team == Team.BLUE) return getPointLocation(Point.BLUE_SPAWN);
        else return Lobby.lobbyLocation;
    }

    /**
     * 게임에 있는 모든 플레이어에게 알림 메세지를 보낸다.
     *
     * @param message 메세지
     */
    public void sendAlertMessage(String message) {
        for (Player player : playerList)
            player.sendMessage(SystemPrefix.CHAT + message);
    }

    public void sendAlertMessage(Player player, String message) {
        player.sendMessage(SystemPrefix.CHAT + message);
    }


    /**
     * 게임에 난입 인원이 필요한지 여부를 리턴한다.
     * 인원이 홀수이면 true 를 반환한다.
     */
    public boolean isNeedPlayer() {
        return (playerList.size() % 2 != 0);
    }
}
