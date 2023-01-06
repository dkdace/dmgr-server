package com.kiwi.dmgr;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 게임의 정보를 담는 클래스
 */
public class Game {

    /** 플레이어 목록 */
    private final ArrayList<Player> playerList;
    /** 플레이어 팀 */
    private HashMap<Player, Team> playerTeam;
    /** 팀 스폰 위치 */
    private HashMap<Team, Location> teamSpawn;
    /** 게임 타입 */
    private GameType type;
    /** 게임 진행 여부 */
    private boolean play;
    /** 전장 맵 */
    private World map;
    /** 랭크 매치 여부 */
    private boolean isRanked;

    /**
     * 매치 큐 인스턴스를 호출하고 {@link GameMapList#gameMap}에 추가한다.
     *
     * <p> 되도록이면 {@link Game#Game(GameType)}을 사용한다.</p>
     */
    public Game() {
        this.playerList = new ArrayList<>();
        this.playerTeam = new HashMap<>();
        this.teamSpawn = new HashMap<>();
        this.type = null;
        this.play = false;
        this.map = null;
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

    public HashMap<Player, Team> getPlayerTeam() {
        return playerTeam;
    }

    public void setPlayerTeam(HashMap<Player, Team> playerTeam) {
        this.playerTeam = playerTeam;
    }

    public HashMap<Team, Location> getTeamSpawn() {
        return teamSpawn;
    }

    public void setTeamSpawn(HashMap<Team, Location> teamSpawn) {
        this.teamSpawn = teamSpawn;
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
        return map;
    }

    public void setMap(World map) {
        this.map = map;
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
        return true;
    }
}
