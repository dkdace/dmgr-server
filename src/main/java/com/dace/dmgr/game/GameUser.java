package com.dace.dmgr.game;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.gui.SelectChar;
import com.dace.dmgr.lobby.Lobby;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.HasTask;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
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
public final class GameUser implements HasTask {
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
    /** 게임 시작 시점 */
    @Getter
    private long startTime = 0;

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

    @Override
    public String getTaskIdentifier() {
        return "GameUser@" + player.getName();
    }

    /**
     * 게임 유저를 초기화하고 틱 스케쥴러를 실행한다.
     */
    public void init() {
        EntityInfoRegistry.addGameUser(player, this);

        TaskManager.addTask(this, new TaskTimer(1) {
            @Override
            public boolean onTimerTick(int i) {
                onTick(i);
                return true;
            }
        });
    }

    /**
     * 매 tick마다 실행할 작업.
     *
     * @param i 인덱스
     */
    private void onTick(int i) {
        CombatUser combatUser = EntityInfoRegistry.getCombatUser(player);
        if (combatUser == null)
            return;

        if (isInSpawnRegion()) {
            if (game.getPhase() == Game.Phase.PLAYING)
                MessageUtil.sendTitle(player, "", combatUser.getCharacterType() == null ? SelectChar.MESSAGES.SELECT_CHARACTER :
                        SelectChar.MESSAGES.CHANGE_CHARACTER, 0, 10, 10);

            player.getInventory().setHeldItemSlot(4);
        } else {
            if (game.getPhase() == Game.Phase.READY || combatUser.getCharacterType() == null)
                player.teleport(getRespawnLocation());
        }
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
        startTime = System.currentTimeMillis();
        player.teleport(getRespawnLocation());
        player.getInventory().setHeldItemSlot(4);
        player.getInventory().setItem(4, Game.SELECT_CHARACTER_ITEM);
        MessageUtil.clearChat(player);

        if (game.getPhase() == Game.Phase.READY)
            MessageUtil.sendTitle(player, game.getGamePlayMode().getName(), SelectChar.MESSAGES.SELECT_CHARACTER, 10,
                    game.getGamePlayMode().getReadyDuration() * 20, 30, 80);
        else
            MessageUtil.sendTitle(player, game.getGamePlayMode().getName(), SelectChar.MESSAGES.SELECT_CHARACTER, 10,
                    40, 30, 80);

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

    /**
     * 플레이어가 팀 스폰 지역에 있는 지 확인한다.
     *
     * @return 팀 스폰 지역에 있으면 {@code true} 반환
     */
    public boolean isInSpawnRegion() {
        if (team == Team.RED)
            return LocationUtil.isInSameBlockXZ(player.getLocation(), GameMap.REGION.SPAWN_REGION_CHECK_Y_COORDINATE, GameMap.REGION.RED_SPAWN_CHECK_BLOCK);
        else if (team == Team.BLUE)
            return LocationUtil.isInSameBlockXZ(player.getLocation(), GameMap.REGION.SPAWN_REGION_CHECK_Y_COORDINATE, GameMap.REGION.BLUE_SPAWN_CHECK_BLOCK);

        return false;
    }
}
