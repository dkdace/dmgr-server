package com.dace.dmgr.game;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.lobby.Lobby;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.util.MessageUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * 게임 시스템의 플레이어 정보를 관리하는 클래스.
 */
@Getter
public final class GameUser {
    /** 플레이어 객체 */
    private final Player player;
    /** 입장한 게임 */
    private final Game game;
    /** 팀 */
    private Team team;
    /** 점수 */
    @Setter
    private double score = 0;
    /** 킬 */
    @Setter
    private int kill = 0;
    /** 데스 */
    @Setter
    private int death = 0;
    /** 어시스트 */
    @Setter
    private int assist = 0;
    /** 입힌 피해량 */
    @Setter
    private int damage = 0;
    /** 막은 피해량 */
    @Setter
    private int defend = 0;
    /** 치유량 */
    @Setter
    private int heal = 0;

    /**
     * 게임 시스템의 플레이어 인스턴스를 생성한다.
     *
     * <p>{@link GameUser#init()}을 호출하여 초기화해야 한다.</p>
     *
     * @param player 대상 플레이어
     */
    public GameUser(Player player, Game game) {
        this.player = player;
        this.game = game;
    }

    /**
     * 게임 유저를 초기화한다.
     */
    public void init() {
        EntityInfoRegistry.addGameUser(player, this);
    }

    /**
     * 게임 유저를 제거한다.
     */
    public void remove() {
        EntityInfoRegistry.removeGameUser(player);
    }

    /**
     * 게임 시작 시 실행할 작업.
     */
    public void onStart() {
        player.teleport(getRespawnLocation());
        player.getInventory().setHeldItemSlot(8);
        player.getInventory().setItem(8, Game.SELECT_CHARACTER_ITEM);
        player.getInventory().setHeldItemSlot(4);
        player.getInventory().setItem(4, Game.SELECT_CHARACTER_ITEM);
        MessageUtil.clearChat(player);

        CombatUser combatUser = EntityInfoRegistry.getCombatUser(player);
        if (combatUser == null) {
            combatUser = new CombatUser(player, this);
            combatUser.init();
        }
    }

    public void setTeam(Team team) {
        this.team = team;

        CombatUser combatUser = EntityInfoRegistry.getCombatUser(player);
        if (combatUser != null && combatUser.getTeam() != team)
            combatUser.setTeam(team);
    }

    /**
     * 플레이어가 속한 팀의 팀 점수를 증가시킨다.
     *
     * @param increment 증가량
     */
    public void addTeamScore(int increment) {
        if (team != Team.NONE)
            game.getTeamScore().put(team, game.getTeamScore().get(team) + increment);
    }

    /**
     * 리스폰 위치를 반환한다.
     *
     * @return 리스폰 위치
     */
    public Location getRespawnLocation() {
        if (team == Team.RED)
            return game.getMap().getRedTeamSpawns()[game.getGamePlayMode().getGamePlayModeScheduler().getRedTeamSpawnIndex()]
                    .toLocation(Bukkit.getWorld(game.getWorldName()));
        else if (team == Team.BLUE)
            return game.getMap().getBlueTeamSpawns()[game.getGamePlayMode().getGamePlayModeScheduler().getBlueTeamSpawnIndex()]
                    .toLocation(Bukkit.getWorld(game.getWorldName()));

        return Lobby.lobbyLocation;
    }

    /**
     * 해당 게임 유저의 킬/데스 를 반환한다.
     *
     * <p>어시스트도 킬로 취급하며, 데스가 {@code 0}이면 {@code 1}로 처리한다.</p>
     *
     * @return (킬 + 어시스트) / 데스
     */
    public float getKDARatio() {
        return (float) (this.getKill() + this.getAssist()) / ((this.getDeath() == 0) ? 1 : this.getDeath());
    }
}