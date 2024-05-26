package com.dace.dmgr.game;

import com.dace.dmgr.Disposable;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Healer;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.GlowUtil;
import com.dace.dmgr.util.HologramUtil;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import com.keenant.tabbed.util.Skins;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Comparator;

/**
 * 게임 시스템의 플레이어 정보를 관리하는 클래스.
 */
public final class GameUser implements Disposable {
    /** 스폰 지역 확인 Y 좌표 */
    private static final int SPAWN_REGION_CHECK_Y_COORDINATE = 41;
    /** 게임 시작 후 탭리스트에 플레이어의 전투원이 공개될 때 까지의 시간 (초) */
    private static final int HEAD_REVEAL_TIME_AFTER_GAME_START = 20;

    /** 플레이어 객체 */
    @NonNull
    @Getter
    private final Player player;
    /** 유저 정보 객체 */
    @NonNull
    @Getter
    private final User user;
    /** 입장한 게임 */
    @NonNull
    @Getter
    private final Game game;
    /** 전투 플레이어 객체 */
    private CombatUser combatUser = null;
    /** 팀 */
    @NonNull
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Team team = Team.NONE;
    /** 점수 */
    @Getter
    @Setter
    private double score = 0;
    /** 킬 */
    @Getter
    @Setter
    private int kill = 0;
    /** 데스 */
    @Getter
    @Setter
    private int death = 0;
    /** 어시스트 */
    @Getter
    @Setter
    private int assist = 0;
    /** 입힌 피해량 */
    @Getter
    @Setter
    private int damage = 0;
    /** 막은 피해량 */
    @Getter
    @Setter
    private int defend = 0;
    /** 치유량 */
    @Getter
    @Setter
    private int heal = 0;
    /** 게임 시작 시점 (타임스탬프) */
    @Getter
    private long startTime = 0;

    /**
     * 게임 시스템의 플레이어 인스턴스를 생성하고, 게임의 소속 유저 목록({@link Game#getGameUsers()})에 추가한다.
     *
     * @param user 대상 플레이어
     * @throws IllegalStateException 해당 {@code user}의 GameUser가 이미 존재하면 발생
     */
    public GameUser(@NonNull User user, @NonNull Game game) {
        GameUser gameUser = GameUserRegistry.getInstance().get(user);
        if (gameUser != null)
            throw new IllegalStateException(MessageFormat.format("플레이어 {0}의 GameUser가 이미 생성됨", user.getPlayer().getName()));

        this.user = user;
        this.player = user.getPlayer();
        this.game = game;

        GameUserRegistry.getInstance().add(user, this);
        game.addPlayer(this);

        TaskUtil.addTask(this, new IntervalTask(i -> {
            onTick(i);
            return true;
        }, 1));
    }

    /**
     * 지정한 플레이어의 게임 유저 인스턴스를 반환한다.
     *
     * @param user 대상 플레이어
     * @return 게임 유저 인스턴스. 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    public static GameUser fromUser(@NonNull User user) {
        return GameUserRegistry.getInstance().get(user);
    }

    /**
     * 매 tick마다 실행할 작업.
     *
     * @param i 인덱스
     */
    private void onTick(long i) {
        if (combatUser == null || game.getPhase() == Game.Phase.WAITING)
            return;

        if (getSpawnRegionTeam() != null) {
            if (game.getPhase() == Game.Phase.PLAYING) {
                if (getSpawnRegionTeam() == team)
                    onTickTeamSpawn();
                else
                    onTickOppositeSpawn();
            }
        } else if (game.getPhase() == Game.Phase.READY || combatUser.getCharacterType() == null)
            user.teleport(getRespawnLocation());

        if (i % 20 == 0)
            updateGameTablist();
    }

    /**
     * 플레이어가 아군 팀 스폰에 있을 때 매 틱마다 실행할 작업.
     */
    private void onTickTeamSpawn() {
        if (game.getPhase() == Game.Phase.PLAYING && !combatUser.isDead())
            user.sendTitle("", (combatUser.getCharacterType() == null) ? "§b§nF키§b를 눌러 전투원을 선택하십시오." :
                    "§b§nF키§b를 눌러 전투원을 변경할 수 있습니다.", 0, 10, 10);

        combatUser.getDamageModule().heal((Healer) null, GeneralConfig.getGameConfig().getTeamSpawnHealPerSecond() / 20, false);
    }

    /**
     * 플레이어가 상대 팀 스폰에 있을 때 매 틱마다 실행할 작업.
     */
    private void onTickOppositeSpawn() {
        if (!combatUser.isDead())
            user.sendTitle("", "§c상대 팀의 스폰 지역입니다.", 0, 10, 10, 20);

        combatUser.getDamageModule().damage(combatUser,
                GeneralConfig.getGameConfig().getOppositeSpawnDamagePerSecond() / 20, DamageType.NORMAL, null, false, false);
    }

    /**
     * 게임 유저를 제거하고, 소속된 게임({@link Game#getGameUsers()})에서 제거한다.
     */
    @Override
    public void dispose() {
        validate();

        GameUserRegistry.getInstance().remove(user);
        if (game.getPhase() == Game.Phase.READY || game.getPhase() == Game.Phase.PLAYING)
            user.getUserData().setQuitCount(user.getUserData().getQuitCount() + 1);
        game.removePlayer(this);
        TaskUtil.clearTask(this);
    }

    @Override
    public boolean isDisposed() {
        return GameUserRegistry.getInstance().get(user) == null;
    }

    /**
     * 게임 시작 시 실행할 작업.
     */
    public void onGameStart() {
        validate();

        startTime = System.currentTimeMillis();
        player.getInventory().setHeldItemSlot(4);
        user.teleport(getRespawnLocation());
        user.clearChat();
        HologramUtil.setHologramVisibility(player.getName(), false, Bukkit.getOnlinePlayers().toArray(new Player[0]));

        user.sendTitle(game.getGamePlayMode().getName(), "§b§nF키§b를 눌러 전투원을 선택하십시오.", 10,
                (game.getPhase() == Game.Phase.READY) ? game.getGamePlayMode().getReadyDuration() * 20 : 40, 30, 80);
        user.clearTabListItems();

        if (combatUser == null)
            combatUser = new CombatUser(this);

        for (GameUser gameUser2 : game.getGameUsers()) {
            if (gameUser2.team != team)
                continue;

            GlowUtil.setGlowing(player, ChatColor.BLUE, gameUser2.player);
            if (gameUser2 != this)
                HologramUtil.setHologramVisibility("hitHealth" + combatUser, true, gameUser2.player);
        }
    }

    /**
     * 게임 탭리스트를 업데이트한다.
     */
    private void updateGameTablist() {
        user.setTabListHeader("\n" + (game.getGamePlayMode().isRanked() ? "§6§l[ 랭크 ] §f" : "§a§l[ 일반 ] §f") + game.getGamePlayMode().getName() +
                "\n" + MessageFormat.format("§4-=-=-=- §c§lRED §f[ {0} ] §4-=-=-=-            §1-=-=-=- §9§lBLUE §f[ {1} ] §1-=-=-=-",
                game.getTeamScore().get(Team.RED), game.getTeamScore().get(Team.BLUE)));

        boolean headReveal = game.getPhase() == Game.Phase.PLAYING &&
                game.getRemainingTime() < game.getGamePlayMode().getPlayDuration() - HEAD_REVEAL_TIME_AFTER_GAME_START;

        int column = 0;
        for (Team team2 : Team.values()) {
            if (team2 == Team.NONE)
                continue;

            column++;
            user.setTabListItem(column, 0, MessageFormat.format("{0}§l§n {1} §f({2}명)",
                    team2.getColor(), team2.getName(), game.getTeamUserMap().get(team2).size()), Skins.getDot(team2.getColor()));

            GameUser[] teamUsers = game.getTeamUserMap().get(team2).stream()
                    .sorted(Comparator.comparing(GameUser::getScore).reversed())
                    .toArray(GameUser[]::new);

            for (int i = 0; i < game.getGamePlayMode().getMaxPlayer() / 2; i++) {
                if (i > teamUsers.length - 1) {
                    user.removeTabListItem(column, (i + 1) * 3 - 2);
                    user.removeTabListItem(column, (i + 1) * 3 - 1);
                } else {
                    user.setTabListItem(column, (i + 1) * 3 - 2, UserData.fromPlayer(teamUsers[i].getPlayer()).getDisplayName(),
                            this.team == team2 || headReveal ? Skins.getPlayer(teamUsers[i].getPlayer()) : Skins.getPlayer("crashdummie99"));
                    user.setTabListItem(column, (i + 1) * 3 - 1, MessageFormat.format("§7✪ §f{0}   §7{1} §f{2}   §7{3} §f{4}   §7{5} §f{6}",
                            teamUsers[i].getScore(), TextIcon.DAMAGE, teamUsers[i].getKill(), TextIcon.POISON, teamUsers[i].getDeath(),
                            "✔", teamUsers[i].getAssist()), null);
                }
            }
        }
    }

    /**
     * 플레이어가 속한 팀의 팀 점수를 증가시킨다.
     *
     * @param increment 증가량
     */
    public void addTeamScore(int increment) {
        validate();

        if (team != Team.NONE)
            game.getTeamScore().put(team, game.getTeamScore().get(team) + increment);
    }

    /**
     * 리스폰 위치를 반환한다.
     *
     * @return 리스폰 위치
     */
    @NonNull
    public Location getRespawnLocation() {
        if (game.getWorld() == null)
            return LocationUtil.getLobbyLocation();
        if (team == Team.RED)
            return game.getMap().getRedTeamSpawns()[game.getGamePlayMode().getGamePlayModeScheduler().getRedTeamSpawnIndex()]
                    .toLocation(game.getWorld());
        else if (team == Team.BLUE)
            return game.getMap().getBlueTeamSpawns()[game.getGamePlayMode().getGamePlayModeScheduler().getBlueTeamSpawnIndex()]
                    .toLocation(game.getWorld());

        return LocationUtil.getLobbyLocation();
    }

    /**
     * 해당 게임 유저의 킬/데스 를 반환한다.
     *
     * <p>어시스트도 킬로 취급하며, 데스가 0이면 1로 처리한다.</p>
     *
     * @return (킬 + 어시스트) / 데스
     */
    public double getKDARatio() {
        return (double) (kill + assist) / ((death == 0) ? 1 : death);
    }

    /**
     * 플레이어가 있는 스폰 지역의 팀을 확인한다.
     *
     * @return 해당 스폰 지역의 팀. 플레이어가 팀 스폰 외부에 있으면 {@code null} 반환
     */
    @Nullable
    public Team getSpawnRegionTeam() {
        if (LocationUtil.isInSameBlockXZ(player.getLocation(), SPAWN_REGION_CHECK_Y_COORDINATE, Material.REDSTONE_ORE))
            return Team.RED;
        else if (LocationUtil.isInSameBlockXZ(player.getLocation(), SPAWN_REGION_CHECK_Y_COORDINATE, Material.LAPIS_ORE))
            return Team.BLUE;

        return null;
    }
}
