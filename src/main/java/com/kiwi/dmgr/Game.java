package com.kiwi.dmgr;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 게임의 정보를 담는 클래스
 */
public class Game {

    /** 플레이어 목록 */
    private final ArrayList<Player> playerList;
    /** 플레이어 팀 */
    private HashMap<Team, ArrayList<Player>> teamPlayerMapList;
    /** 팀 스폰 위치 */
    private HashMap<Team, Location> teamSpawnLocation;
    /** 팀 스코어 */
    private HashMap<Team, Integer> teamScore;
    /** 잔여 시간 */
    private long remainTime;
    /** 게임 타입 */
    private GameType type;
    /** 게임 진행 여부 */
    private boolean play;
    /** 전장 맵 */
    private World world;
    /** 랭크 매치 여부 */
    private boolean isRanked;

    /**
     * 매치 큐 인스턴스를 호출하고 {@link GameMapList#gameMap}에 추가한다.
     */
    public Game() {
        this.playerList = new ArrayList<>();
        this.teamPlayerMapList = new HashMap<>();
        this.teamSpawnLocation = new HashMap<>();
        this.teamScore = new HashMap<>();
        this.teamScore = new HashMap<>();
        this.remainTime = 0;
        this.type = null;
        this.play = false;
        this.world = null;
        this.isRanked = false;
    }

    public Game(GameType type) {
        super();
        this.playerList = null;
        this.type = type;

        GameMapList.gameAdd(this);
    }

    /**
     * 게임에 플레이어를 추가한다.
     * 시작 가능 인원이 되었을경우 {@link GameManager#}
     *
     * @param player 플레이어
     */
    public void addPlayer(Player player) {
        this.playerList.add(player);
    }

    public ArrayList<Player> getPlayerList() {
        return this.playerList;
    }

    public ArrayList<Player> getTeamPlayerList(Team team) {
        return teamPlayerMapList.get(team);
    }

    public void setTeamPlayerList(Team team, ArrayList<Player> playerList) {
        this.teamPlayerMapList.put(team, playerList);
    }

    public Location getTeamSpawn(Team team) {
        return this.teamSpawnLocation.get(team);
    }

    public void setTeamSpawn(Team team, Location location) {
        this.teamSpawnLocation.put(team, location);
    }

    public int getTeamScore(Team team) {
        return this.teamScore.get(team);
    }

    public void setTeamScore(Team team, int score) {
        this.teamScore.put(team, score);
    }

    public long getRemainTime() {
        return remainTime;
    }

    public void setRemainTime(long remainTime) {
        this.remainTime = remainTime;
    }

    public GameType getType() {
        return type;
    }

    public void setType(GameType type) {
        this.type = type;
    }

    public boolean isPlay() {
        return play;
    }

    public void setPlay(boolean play) {
        this.play = play;
    }

    public World getMap() {
        return world;
    }

    public void setMap(World world) {
        this.world = world;
    }

    public boolean isRanked() {
        return isRanked;
    }

    public void setRanked(boolean ranked) {
        isRanked = ranked;
    }

    public boolean canStart() {
        return playerList.size() % 2 == 0;
    }

    /**
     * 게임이 난입 인원이 필요한지 여부를 리턴한다.
     *
     * @return boolean 인원 필요 여부
     */
    public boolean isNeedPlayer() {
        return false;
    }
}
