package com.dace.dmgr.game;

import com.comphenix.packetwrapper.WrapperPlayServerBoss;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.lobby.Lobby;
import com.dace.dmgr.lobby.UserData;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.GameInfoRegistry;
import com.dace.dmgr.system.SystemPrefix;
import com.dace.dmgr.system.task.HasTask;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.system.task.TaskWait;
import com.dace.dmgr.util.BossBarUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.WorldUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 게임의 정보와 진행을 관리하는 클래스.
 */
public final class Game implements HasTask {
    /** 전투원 선택 아이템 객체 */
    public static final ItemStack SELECT_CHARACTER_ITEM = new ItemBuilder(Material.EMERALD)
            .setName("§a전투원 선택").build();

    private static final Random random = new Random();

    /** 방 번호 */
    @Getter
    private final int number;
    /** 엔티티 목록 */
    private final HashSet<CombatEntity> combatEntitySet = new HashSet<>();
    /** 플레이어 목록 */
    @Getter
    private final ArrayList<GameUser> gameUsers = new ArrayList<>();
    /** 게임을 시작하기 위한 최소 인원 수 */
    private final int minPlayerCount;
    /** 최대 수용 가능 인원 수 */
    private final int maxPlayerCount;
    /** 팀별 플레이어 목록 (팀 : 플레이어 목록) */
    @Getter
    private final EnumMap<Team, ArrayList<GameUser>> teamUserMap = new EnumMap<>(Team.class);
    /** 팀 점수 (팀 : 점수) */
    @Getter
    private final EnumMap<Team, Integer> teamScore = new EnumMap<>(Team.class);
    /** 게임 모드 */
    @Getter
    private final GamePlayMode gamePlayMode;
    /** 전장 월드 이름 */
    @Getter
    private final String worldName;
    /** 맵 */
    @Getter
    private final GameMap map;
    /** 게임 시작 시점 ({@link Phase#READY}가 된 시점) */
    @Getter
    private long startTime = 0;
    /** 다음 진행 단계까지 남은 시간 */
    @Getter
    private int remainingTime = GameConfig.WAITING_TIME;
    /** 게임 진행 단계 */
    @Getter
    private Phase phase = Phase.WAITING;

    /**
     * 게임 인스턴스를 생성한다.
     *
     * @param isRanked 랭크 여부
     * @param number   방 번호
     */
    public Game(boolean isRanked, int number) {
        this.number = number;
        this.gamePlayMode = GameInfoRegistry.getRandomGamePlayMode(isRanked);
        this.map = GameInfoRegistry.getRandomMap(gamePlayMode);
        this.worldName = MessageFormat.format("_{0}-{1}-{2}", map.getWorldName(), gamePlayMode, number);
        minPlayerCount = isRanked ? GameConfig.RANK_MIN_PLAYER_COUNT : GameConfig.NORMAL_MIN_PLAYER_COUNT;
        maxPlayerCount = isRanked ? GameConfig.RANK_MAX_PLAYER_COUNT : GameConfig.NORMAL_MAX_PLAYER_COUNT;
        for (Team team : Team.values()) {
            this.teamUserMap.put(team, new ArrayList<>());
            this.teamScore.put(team, 0);
        }
    }

    /**
     * 게임 정보를 초기화하고 스케쥴러를 실행한다.
     */
    public void init() {
        GameInfoRegistry.addGame(this);

        TaskManager.addTask(this, new TaskTimer(20) {
            @Override
            protected boolean onTimerTick(int i) {
                onSecond();
                return true;
            }
        });
    }

    /**
     * 게임을 제거한다.
     */
    public void remove() {
        if (!gameUsers.isEmpty())
            for (GameUser gameUser : new ArrayList<>(gameUsers)) {
                Lobby.spawn(gameUser.getPlayer());
                removePlayer(gameUser.getPlayer());
            }

        unloadWorld();
        GameInfoRegistry.removeGame(this);
        TaskManager.clearTask(this);
    }

    @Override
    public String getTaskIdentifier() {
        return "Game@" + gamePlayMode.toString() + hashCode();
    }

    /**
     * @return 게임에 속한 모든 엔티티
     */
    public CombatEntity[] getAllCombatEntities() {
        return combatEntitySet.toArray(new CombatEntity[0]);
    }

    /**
     * @param combatEntity 전투 시스템의 엔티티 객체
     */
    public void addCombatEntity(CombatEntity combatEntity) {
        combatEntitySet.add(combatEntity);
    }

    /**
     * @param combatEntity 전투 시스템의 엔티티 객체
     */
    public void removeCombatEntity(CombatEntity combatEntity) {
        combatEntitySet.remove(combatEntity);
    }

    /**
     * 월드를 로드한다.
     */
    private void loadWorld() {
        WorldUtil.duplicateWorld(map.getWorldName(), worldName);
    }

    /**
     * 월드를 언로드한다.
     */
    private void unloadWorld() {
        WorldUtil.removeWorld(worldName);
    }

    /**
     * 매 초마다 실행할 작업.
     */
    private void onSecond() {
        remainingTime--;

        switch (phase) {
            case WAITING: {
                onSecondWaiting();

                break;
            }
            case READY: {
                onSecondReady();

                break;
            }
            case PLAYING: {
                onSecondPlaying();

                break;
            }
            case END:
                remove();
        }
    }

    /**
     * 진행 단계가 {@link Phase#WAITING}일 때 매 초마다 실행할 작업.
     */
    private void onSecondWaiting() {
        gameUsers.forEach(this::sendBossBarWaiting);

        if (canStart()) {
            if (remainingTime == 3)
                loadWorld();

            if (remainingTime > 0 && (remainingTime <= 5 || remainingTime == 10))
                gameUsers.forEach(gameUser -> sendMessage(gameUser, MessageFormat.format("게임이 {0}초 뒤에 시작합니다.", remainingTime)));

            if (remainingTime == 0) {
                phase = Phase.READY;
                remainingTime = gamePlayMode.getReadyDuration();
                gameUsers.forEach(gameUser -> BossBarUtil.clearBossBar(gameUser.getPlayer()));

                onStart();
            }
        } else
            remainingTime = GameConfig.WAITING_TIME;
    }

    /**
     * 플레이어에게 게임 시작 대기 보스바를 전송한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void sendBossBarWaiting(GameUser gameUser) {
        BossBarUtil.addBossBar(gameUser.getPlayer(), "waitQuit", MESSAGES.BOSSBAR_WAIT_QUIT,
                BarColor.WHITE, WrapperPlayServerBoss.BarStyle.PROGRESS, 0);
        BossBarUtil.addBossBar(gameUser.getPlayer(), "cannotStart", MessageFormat.format(MESSAGES.BOSSBAR_CANNOT_START,
                minPlayerCount), BarColor.WHITE, WrapperPlayServerBoss.BarStyle.PROGRESS, 0);
        BossBarUtil.addBossBar(gameUser.getPlayer(), "timer",
                MessageFormat.format(MESSAGES.BOSSBAR_TIMER,
                        (gamePlayMode.isRanked() ? "§6§l랭크" : "§a§l일반"), (canStart() ? "§f" : "§c") + gameUsers.size(),
                        maxPlayerCount),
                BarColor.GREEN,
                WrapperPlayServerBoss.BarStyle.PROGRESS,
                canStart() ? (float) remainingTime / GameConfig.WAITING_TIME : 1);
    }

    /**
     * 진행 단계가 {@link Phase#READY}일 때 매 초마다 실행할 작업.
     */
    private void onSecondReady() {
        if (remainingTime > 0 && remainingTime <= 5) {
            gameUsers.forEach(gameUser -> {
                SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F, gameUser.getPlayer());
                gameUser.getPlayer().sendTitle("§f" + remainingTime, "", 0, 5, 10);
            });
        }

        if (remainingTime == 0) {
            phase = Phase.PLAYING;
            remainingTime = gamePlayMode.getPlayDuration();

            gameUsers.forEach(gameUser -> {
                SoundUtil.play(Sound.ENTITY_WITHER_SPAWN, 10F, 1F, gameUser.getPlayer());
                gameUser.getPlayer().sendTitle("§c전투 시작", "", 0, 40, 20);
            });
        }
    }

    /**
     * 진행 단계가 {@link Phase#PLAYING}일 때 매 초마다 실행할 작업.
     */
    private void onSecondPlaying() {
        gamePlayMode.getGamePlayModeScheduler().onSecond(this);

        if (remainingTime > 0 && remainingTime <= 10) {
            gameUsers.forEach(gameUser -> {
                SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F, gameUser.getPlayer());
                gameUser.getPlayer().sendTitle("", "§c" + remainingTime, 0, 5, 10);
            });
        }

        if (remainingTime == 0) {
            phase = Phase.END;
            onFinish();
        }
    }

    /**
     * 게임을 시작할 조건이 충족되었는 지 확인한다.
     *
     * @return 게임을 시작할 수 있으면 {@code true} 반환
     */
    public boolean canStart() {
        return gameUsers.size() >= minPlayerCount && (gameUsers.size() % 2 == 0);
    }

    /**
     * 게임 시작 시 실행할 작업.
     */
    private void onStart() {
        startTime = System.currentTimeMillis();
        gameUsers.forEach(gameUser -> {
            EntityInfoRegistry.getUser(gameUser.getPlayer()).clearChat();
            gameUser.getPlayer().sendTitle(gamePlayMode.getName(), "", 10, gamePlayMode.getReadyDuration() * 20, 0);
            CombatUser combatUser = new CombatUser(gameUser.getPlayer(), gameUser);
            combatUser.init();
        });
        divideTeam();

        gameUsers.forEach(GameUser::onStart);
    }

    /**
     * 게임 참여자들의 MMR에 따라 팀을 나눈다.
     *
     * <p>팀 분배는 다음과 같이 이뤄진다. (플레이어 수를 i라고 하자.)</p>
     *
     * <ol>
     * <li>먼저 최상위 플레이어를 일부 얻는다. (i / 4의 내림값의 인원수)</li>
     * <li>그 최상위 플레이어를 1팀으로 이동시킨다.</li>
     * <li>그리고 중위권 플레이어를 2팀에 전부 이동시킨다.</li>
     * <li>나머지 하위권 플레이어를 1팀으로 전부 이동시킨다.</li>
     * <li>1팀, 2팀을 무작위로 {@link Team#RED} 또는 {@link Team#BLUE}로 지정한다.</li>
     * </ol>
     */
    private void divideTeam() {
        List<GameUser> sortedGameUsers = gameUsers.stream()
                .sorted(Comparator.comparing(gameUser ->
                        EntityInfoRegistry.getUser(((GameUser) gameUser).getPlayer()).getUserData().getMatchMakingRate()).reversed())
                .collect(Collectors.toList());
        ArrayList<GameUser> team1 = new ArrayList<>();
        ArrayList<GameUser> team2 = new ArrayList<>();
        int size = sortedGameUsers.size();

        for (int i = 0; i < size / 4; i++) {
            team1.add(sortedGameUsers.get(0));
            sortedGameUsers.remove(0);
        }
        for (int i = 0; i < size / 2; i++) {
            team2.add(sortedGameUsers.get(0));
            sortedGameUsers.remove(0);
        }
        team1.addAll(sortedGameUsers);

        Team team1Team;
        Team team2Team;
        if (random.nextBoolean()) {
            team1Team = Team.RED;
            team2Team = Team.BLUE;
        } else {
            team1Team = Team.BLUE;
            team2Team = Team.RED;
        }

        team1.forEach(gameUser -> {
            gameUser.setTeam(team1Team);
            teamUserMap.get(team1Team).add(gameUser);
        });
        team2.forEach(gameUser -> {
            gameUser.setTeam(team2Team);
            teamUserMap.get(team2Team).add(gameUser);
        });
    }

    /**
     * 게임 종료 시 실행할 작업.
     */
    private void onFinish() {
        gameUsers.forEach(gameUser -> EntityInfoRegistry.getUser(gameUser.getPlayer()).clearChat());

        Team winnerTeam = teamScore.get(Team.RED) > teamScore.get(Team.BLUE) ? Team.RED : Team.BLUE;
        if (teamScore.get(Team.RED).equals(teamScore.get(Team.BLUE)))
            winnerTeam = Team.NONE;

        for (GameUser gameUser : gameUsers) {
            Boolean isWinner = winnerTeam == Team.NONE ? null : gameUser.getTeam() == winnerTeam;

            int moneyEarned = updateMoney(gameUser, isWinner);
            int xpEarned = updateXp(gameUser, isWinner);
            int rankEarned = 0;

            if (gamePlayMode.isRanked())
                rankEarned = updateRankRate(gameUser, isWinner);
            else
                updateMMR(gameUser);
        }

        for (GameUser gameUser : gameUsers) {
            sendMessage(gameUser, gamePlayMode.getName() + " 게임 결과");

            if (winnerTeam == Team.NONE)
                sendMessage(gameUser, "* " + winnerTeam.getColor() + "무승부");
            else
                sendMessage(gameUser, "* " + winnerTeam.getColor() + winnerTeam.name() + "팀 승리");
            sendMessage(gameUser, "");

            sendMessage(gameUser, "내 게임 성적");
            sendMessage(gameUser, "* 점수: " + gameUser.getScore());
            sendMessage(gameUser, "* K/D/A: " + gameUser.getKill() + "/" + gameUser.getDeath() + "/" + gameUser.getAssist() +
                    " (" + (gameUser.getKDARatio()) + ")");
            sendMessage(gameUser, "* 입힌 데미지: " + gameUser.getDamage());
        }
    }

    /**
     * 매치 종료 후 결과에 따라 플레이어의 경험치를 증가시킨다.
     *
     * @param gameUser 대상 플레이어
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 획득한 경험치
     */
    private int updateXp(GameUser gameUser, Boolean isWinner) {
        UserData userData = EntityInfoRegistry.getUser(gameUser.getPlayer()).getUserData();

        int xp = userData.getXp();
        double score = gameUser.getScore();

        userData.setXp(RewardUtil.getFinalXp(xp, score, isWinner));

        return userData.getXp() - xp;
    }

    /**
     * 매치 종료 후 결과에 따라 플레이어의 돈을 증가시킨다.
     *
     * @param gameUser 대상 플레이어
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 획득한 돈
     */
    private int updateMoney(GameUser gameUser, Boolean isWinner) {
        UserData userData = EntityInfoRegistry.getUser(gameUser.getPlayer()).getUserData();

        int xp = userData.getXp();
        int money = userData.getMoney();
        double score = gameUser.getScore();

        userData.setMoney(RewardUtil.getFinalMoney(xp, money, score, isWinner));

        return userData.getMoney() - money;
    }

    /**
     * 일반 매치 종료 후 결과에 따라 플레이어의 MMR을 조정한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void updateMMR(GameUser gameUser) {
        UserData userData = EntityInfoRegistry.getUser(gameUser.getPlayer()).getUserData();

        int mmr = userData.getMatchMakingRate();
        int normalPlayCount = userData.getNormalPlayCount();
        float kda = gameUser.getKDARatio();
        double score = gameUser.getScore();
        int playTime = (int) ((System.currentTimeMillis() - startTime) / 1000);
        int gameAverageMMR = (int) gameUser.getGame().getAverageMMR();

        userData.setMatchMakingRate(RewardUtil.getFinalMMR(mmr, normalPlayCount, kda, score, playTime, gameAverageMMR));
        userData.setNormalPlayCount(normalPlayCount + 1);

        DMGR.getPlugin().getLogger().info(MessageFormat.format("유저 MMR 변동됨: ({0}) {1} -> {2} MMR PLAY: {3}",
                gameUser.getPlayer().getName(), mmr, userData.getMatchMakingRate(), normalPlayCount + 1));
    }

    /**
     * 랭크 매치 종료 후 결과에 따라 플레이어의 랭크 점수와 MMR을 조정한다.
     *
     * @param gameUser 대상 플레이어
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 랭크 점수 획득량
     */
    private int updateRankRate(GameUser gameUser, Boolean isWinner) {
        UserData userData = EntityInfoRegistry.getUser(gameUser.getPlayer()).getUserData();

        int mmr = userData.getMatchMakingRate();
        int rr = userData.getRankRate();
        int rankPlayCount = userData.getRankPlayCount();
        float kda = gameUser.getKDARatio();
        double score = gameUser.getScore();
        int playTime = (int) ((System.currentTimeMillis() - startTime) / 1000);
        int gameAverageMMR = (int) gameUser.getGame().getAverageMMR();
        int gameAverageRank = (int) gameUser.getGame().getAverageRankRate();

        userData.setMatchMakingRate(RewardUtil.getFinalMMR(mmr, rankPlayCount, kda, score, playTime, gameAverageMMR));
        userData.setRankPlayCount(rankPlayCount + 1);

        if (!userData.isRanked()) {
            if (rankPlayCount + 1 >= GameConfig.RANK_PLACEMENT_PLAY_COUNT) {
                userData.setRankRate(RewardUtil.getFinalRankRate(mmr));
                userData.setRanked(true);
            }
        } else
            userData.setRankRate(RewardUtil.getFinalRankRateRanked(mmr, rr, kda, score, playTime, gameAverageRank, isWinner));

        return userData.getRankRate() - rr;
    }

    /**
     * 플레이어가 게임에 참여할 수 있는 지 확인한다.
     *
     * @return 참여 가능 여부
     */
    public boolean canJoin() {
        if (gameUsers.size() >= maxPlayerCount)
            return false;

        int redAmount = teamUserMap.get(Team.RED).size();
        int blueAmount = teamUserMap.get(Team.BLUE).size();

        if (phase != Phase.WAITING)
            return !gamePlayMode.isRanked() && redAmount != blueAmount;

        return true;
    }

    /**
     * 게임에 지정한 플레이어를 추가한다.
     *
     * <p>게임이 진행 중이면 인원이 부족한 팀이 있을 때만 난입이 가능하다.</p>
     *
     * @param player 대상 플레이어
     */
    public void addPlayer(Player player) {
        if (!canJoin())
            return;

        GameUser gameUser = new GameUser(player, this);
        gameUser.init();

        int redAmount = teamUserMap.get(Team.RED).size();
        int blueAmount = teamUserMap.get(Team.BLUE).size();

        if (phase != Phase.WAITING) {
            if (redAmount < blueAmount)
                gameUser.setTeam(Team.RED);
            else
                gameUser.setTeam(Team.BLUE);

            teamUserMap.get(gameUser.getTeam()).add(gameUser);
            gameUser.onStart();
        }

        gameUsers.add(gameUser);
        gameUsers.forEach(gameUser2 -> sendMessage(gameUser2, MESSAGES.JOIN_PREFIX + gameUser.getPlayer().getName()));
    }

    /**
     * 게임에서 지정한 플레이어를 제거한다.
     *
     * @param player 대상 플레이어
     */
    public void removePlayer(Player player) {
        GameUser gameUser = EntityInfoRegistry.getGameUser(player);
        if (gameUser == null)
            return;

        gameUser.remove();
        BossBarUtil.clearBossBar(player);

        gameUsers.forEach(gameUser2 -> sendMessage(gameUser2, MESSAGES.QUIT_PREFIX + gameUser.getPlayer().getName()));
        gameUsers.remove(gameUser);
        if (teamUserMap.get(gameUser.getTeam()) != null)
            teamUserMap.get(gameUser.getTeam()).remove(gameUser);

        if (phase != Phase.END && gameUsers.isEmpty())
            phase = Phase.END;
    }

    /**
     * 지정한 플레이어에게 게임 메시지를 전송한다.
     *
     * @param message 메시지
     */
    public void sendMessage(GameUser gameUser, String message) {
        Player player = gameUser.getPlayer();
        player.sendMessage(SystemPrefix.CHAT + message);
    }

    /**
     * 게임에 참여한 모든 플레이어의 MMR 평균을 반환한다.
     *
     * @return 참여자들의 MMR 평균
     */
    public double getAverageMMR() {
        return gameUsers.stream()
                .mapToInt(gameUser -> EntityInfoRegistry.getUser(gameUser.getPlayer()).getUserData().getMatchMakingRate())
                .average()
                .orElse(0);
    }

    /**
     * 게임에 참여한 모든 플레이어의 랭크 점수 평균을 반환한다.
     *
     * @return 참여자들의 랭크 점수 평균
     */
    public double getAverageRankRate() {
        return gameUsers.stream()
                .mapToInt(gameUser -> EntityInfoRegistry.getUser(gameUser.getPlayer()).getUserData().getRankRate())
                .average()
                .orElse(0);
    }

    /**
     * 게임 진행 단계 목록.
     */
    @AllArgsConstructor
    @Getter
    public enum Phase {
        /** 인원 대기 */
        WAITING("대기 중"),
        /** 준비 */
        READY("게임 준비"),
        /** 진행 */
        PLAYING("게임 진행"),
        /** 종료 */
        END("종료됨");

        private final String name;
    }

    /**
     * 게임에 사용되는 메시지 목록.
     */
    private interface MESSAGES {
        /** 입장 메시지의 접두사 */
        String JOIN_PREFIX = "§f§l[§a§l+§f§l] §b";
        /** 퇴장 메시지의 접두사 */
        String QUIT_PREFIX = "§f§l[§6§l-§f§l] §b";
        /** 대기열 나가기 보스바 메시지 */
        String BOSSBAR_WAIT_QUIT = "§f대기열에서 나가려면 §n'/quit'§f 또는 §n'/q'§f를 입력하십시오.";
        /** 게임 시작 불가 보스바 메시지 */
        String BOSSBAR_CANNOT_START = "§c게임을 시작하려면 최소 {0}명이 필요합니다.";
        /** 게임 시작 타이머 보스바 메시지 */
        String BOSSBAR_TIMER = "§a§l{0} §f[{1}§f/{2} 명]";
    }
}
