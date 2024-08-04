package com.dace.dmgr.game;

import com.comphenix.packetwrapper.WrapperPlayServerBoss;
import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.Disposable;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.WorldUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.*;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 게임의 정보와 진행을 관리하는 클래스.
 */
public final class Game implements Disposable {
    /** 게임 시작 대기 보스바 ID */
    private static final String GAME_WAIT_BOSSBAR_ID = "GameWait";

    /** 방 번호 */
    @Getter
    private final int number;
    /** 소속된 엔티티 목록 */
    private final HashSet<CombatEntity> combatEntities = new HashSet<>();
    /** 소속된 플레이어 목록 */
    private final ArrayList<GameUser> gameUsers = new ArrayList<>();
    /** 게임을 시작하기 위한 최소 인원 수 */
    private final int minPlayerCount;
    /** 최대 수용 가능 인원 수 */
    private final int maxPlayerCount;
    /** 팀 목록 (색상 : 팀) */
    @NonNull
    @Getter
    private final EnumMap<ChatColor, Team> teams = new EnumMap<>(ChatColor.class);
    /** 게임 모드 */
    @NonNull
    @Getter
    private final GamePlayMode gamePlayMode;
    /** 맵 */
    @NonNull
    @Getter
    private final GameMap map;
    /** 전장 월드 */
    @Nullable
    @Getter
    private World world;
    /** 게임 시작 시점 ({@link Phase#READY}가 된 시점, 타임스탬프) */
    @Getter
    private long startTime = 0;
    /** 다음 진행 단계까지 남은 시간 (초) */
    @Getter
    private int remainingTime = GeneralConfig.getGameConfig().getWaitingTimeSeconds();
    /** 게임 진행 단계 */
    @NonNull
    @Getter
    private Phase phase = Phase.WAITING;

    /**
     * 게임 인스턴스를 생성한다.
     *
     * @param isRanked 랭크 여부
     * @param number   방 번호
     * @throws IllegalStateException 해당 {@code isRanked}, {@code number}의 Game이 이미 존재하면 발생
     */
    public Game(boolean isRanked, int number) {
        Game game = GameRegistry.getInstance().get(new GameRegistry.KeyPair(isRanked, number));
        if (game != null)
            throw new IllegalStateException(MessageFormat.format("랭크 여부 {0}, 방 번호 {1}의 Game이 이미 생성됨", isRanked, number));

        this.number = number;
        this.gamePlayMode = GamePlayMode.getRandomGamePlayMode(isRanked);
        this.map = gamePlayMode.getRandomMap();
        minPlayerCount = isRanked ? GeneralConfig.getGameConfig().getRankMinPlayerCount() : GeneralConfig.getGameConfig().getNormalMinPlayerCount();
        maxPlayerCount = isRanked ? GeneralConfig.getGameConfig().getRankMaxPlayerCount() : GeneralConfig.getGameConfig().getNormalMaxPlayerCount();
        teams.put(ChatColor.RED, new Team(ChatColor.RED, "레드"));
        teams.put(ChatColor.BLUE, new Team(ChatColor.BLUE, "블루"));
        GameRegistry.getInstance().add(new GameRegistry.KeyPair(isRanked, number), this);

        TaskUtil.addTask(this, new IntervalTask(i -> {
            onSecond();
            return true;
        }, 20));
    }

    /**
     * 지정한 번호의 게임 인스턴스를 반환한다.
     *
     * @param isRanked 랭크 여부
     * @param number   방 번호
     * @return 게임 인스턴스. 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    public static Game fromNumber(boolean isRanked, int number) {
        return GameRegistry.getInstance().get(new GameRegistry.KeyPair(isRanked, number));
    }

    @Override
    public void dispose() {
        validate();

        if (!gameUsers.isEmpty())
            for (GameUser gameUser : new ArrayList<>(gameUsers)) {
                if (phase != Phase.WAITING)
                    gameUser.getUser().reset();

                gameUser.dispose();
            }

        if (world == null)
            onDispose();
        else
            TaskUtil.addTask(this, WorldUtil.removeWorld(world).onFinish(this::onDispose));
    }

    /**
     * 게임 폐기 완료 시 실행할 작업.
     */
    private void onDispose() {
        world = null;
        GameRegistry.getInstance().remove(new GameRegistry.KeyPair(gamePlayMode.isRanked(), number));
        TaskUtil.clearTask(this);
    }

    @Override
    public boolean isDisposed() {
        return GameRegistry.getInstance().get(new GameRegistry.KeyPair(gamePlayMode.isRanked(), number)) == null;
    }

    /**
     * 게임에 소속된 모든 엔티티 목록을 반환한다.
     *
     * @return 게임에 속한 모든 엔티티
     */
    @NonNull
    public CombatEntity @NonNull [] getAllCombatEntities() {
        return combatEntities.toArray(new CombatEntity[0]);
    }

    /**
     * 게임에 지정한 엔티티를 추가한다.
     *
     * @param combatEntity 전투 시스템의 엔티티 객체
     */
    public void addCombatEntity(@NonNull CombatEntity combatEntity) {
        validate();
        combatEntities.add(combatEntity);
    }

    /**
     * 게임에 소속된 지정한 엔티티를 제거한다.
     *
     * @param combatEntity 전투 시스템의 엔티티 객체
     */
    public void removeCombatEntity(@NonNull CombatEntity combatEntity) {
        validate();
        combatEntities.remove(combatEntity);
    }

    @NonNull
    public GameUser @NonNull [] getGameUsers() {
        return gameUsers.toArray(new GameUser[0]);
    }

    /**
     * 게임 진행 중 매 초마다 실행할 작업.
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
                dispose();
        }
    }

    /**
     * 진행 단계가 {@link Phase#WAITING}일 때 매 초마다 실행할 작업.
     */
    private void onSecondWaiting() {
        gameUsers.forEach(this::sendBossBarWaiting);

        if (!canStart()) {
            remainingTime = GeneralConfig.getGameConfig().getWaitingTimeSeconds();
            return;
        }

        if (remainingTime > 0 && (remainingTime <= 5 || remainingTime == 10))
            gameUsers.forEach(gameUser -> gameUser.getUser()
                    .sendMessageInfo("게임이 {0}초 뒤에 시작합니다.", remainingTime));

        if (remainingTime == 0) {
            phase = Phase.READY;
            onReady();
        }
    }

    /**
     * 플레이어에게 게임 시작 대기 보스바를 전송한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void sendBossBarWaiting(@NonNull GameUser gameUser) {
        gameUser.getUser().addBossBar(GAME_WAIT_BOSSBAR_ID + '1', "§f대기열에서 나가려면 §n'/quit'§f 또는 §n'/q'§f를 입력하십시오.",
                BarColor.WHITE, WrapperPlayServerBoss.BarStyle.PROGRESS, 0);
        gameUser.getUser().addBossBar(GAME_WAIT_BOSSBAR_ID + '2',
                MessageFormat.format("§c게임을 시작하려면 최소 {0}명이 필요합니다.", minPlayerCount),
                BarColor.WHITE, WrapperPlayServerBoss.BarStyle.PROGRESS, 0);
        gameUser.getUser().addBossBar(GAME_WAIT_BOSSBAR_ID + '3',
                MessageFormat.format("§a§l{0} §f[{1}§f/{2} 명]",
                        (gamePlayMode.isRanked() ? "§6§l랭크" : "§a§l일반"), (canStart() ? "§f" : "§c") + gameUsers.size(),
                        maxPlayerCount),
                BarColor.GREEN,
                WrapperPlayServerBoss.BarStyle.PROGRESS,
                canStart() ? (double) remainingTime / GeneralConfig.getGameConfig().getWaitingTimeSeconds() : 1);
    }

    /**
     * 진행 단계가 {@link Phase#READY}일 때 매 초마다 실행할 작업.
     */
    private void onSecondReady() {
        if (remainingTime > 0 && remainingTime <= 5) {
            gameUsers.forEach(gameUser -> {
                SoundUtil.playNamedSound(NamedSound.GAME_TIMER, gameUser.getPlayer());
                gameUser.getUser().sendTitle("§f" + remainingTime, "", 0, 5, 10, 10);
            });
        }

        if (remainingTime == 0) {
            phase = Phase.PLAYING;
            remainingTime = gamePlayMode.getPlayDuration();

            gameUsers.forEach(gameUser -> {
                SoundUtil.playNamedSound(NamedSound.GAME_ON_PLAY, gameUser.getPlayer());
                gameUser.getUser().sendTitle("§c§l전투 시작", "", 0, 40, 20, 40);
                gameUser.getUser().sendMessageInfo("");
                gameUser.getUser().sendMessageInfo("§n'/전체'§r 또는 §n'/tc'§r를 입력하여 팀/전체 채팅을 전환할 수 있습니다.");
                gameUser.getUser().sendMessageInfo("");
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
                SoundUtil.playNamedSound(NamedSound.GAME_TIMER, gameUser.getPlayer());
                gameUser.getUser().sendTitle("", "§c" + remainingTime, 0, 5, 10, 10);
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
     * 게임 준비 시 실행할 작업.
     */
    private void onReady() {
        gameUsers.forEach(gameUser -> gameUser.getUser().sendMessageInfo("월드 불러오는 중..."));

        WorldUtil.duplicateWorld(map.getWorld(), MessageFormat.format("_{0}-{1}-{2}",
                map.getWorld().getName(), gamePlayMode, number)).onFinish(newWorld -> {
            world = newWorld;
            remainingTime = gamePlayMode.getReadyDuration();
            startTime = System.currentTimeMillis();
            divideTeam();

            gameUsers.forEach(gameUser -> {
                gameUser.getUser().clearBossBar();
                gameUser.onGameStart();
            });
        }).onError(ex -> {
            gameUsers.forEach(gameUser2 -> gameUser2.getUser()
                    .sendMessageWarn("오류로 인해 월드를 불러올 수 없습니다. 관리자에게 문의하십시오."));
            phase = Phase.END;
        });
    }

    /**
     * 게임 참여자들의 MMR에 따라 팀을 나눈다.
     *
     * <p>팀 분배는 다음과 같이 이뤄진다.</p>
     *
     * <ol>
     * <li>플레이어 수를 i라고 하자. 먼저 최상위 플레이어를 일부 얻는다. (i / 4의 내림값의 인원수)</li>
     * <li>그 최상위 플레이어를 1팀으로 이동시킨다.</li>
     * <li>그리고 중위권 플레이어를 2팀에 전부 이동시킨다.</li>
     * <li>나머지 하위권 플레이어를 1팀으로 전부 이동시킨다.</li>
     * <li>1팀, 2팀을 무작위로 레드팀 또는 블루팀으로 지정한다.</li>
     * </ol>
     */
    private void divideTeam() {
        LinkedList<GameUser> sortedGameUsers = gameUsers.stream()
                .sorted(Comparator.comparing((GameUser gameUser) ->
                        gameUser.getUser().getUserData().getMatchMakingRate()).reversed())
                .collect(Collectors.toCollection(LinkedList::new));
        HashSet<GameUser> team1 = new HashSet<>();
        HashSet<GameUser> team2 = new HashSet<>();
        int size = sortedGameUsers.size();

        for (int i = 0; i < size / 4; i++)
            team1.add(sortedGameUsers.pop());
        for (int i = 0; i < size / 2; i++)
            team2.add(sortedGameUsers.pop());
        team1.addAll(sortedGameUsers);

        Team team1Team = teams.get(ChatColor.RED);
        Team team2Team = teams.get(ChatColor.BLUE);
        if (DMGR.getRandom().nextBoolean()) {
            team1Team = teams.get(ChatColor.BLUE);
            team2Team = teams.get(ChatColor.RED);
        }

        for (GameUser gameUser : team1) {
            gameUser.setTeam(team1Team);
            team1Team.teamUsers.add(gameUser);
        }
        for (GameUser gameUser : team2) {
            gameUser.setTeam(team2Team);
            team2Team.teamUsers.add(gameUser);
        }
    }

    /**
     * 게임 종료 시 실행할 작업.
     */
    private void onFinish() {
        Team winnerTeam = teams.get(ChatColor.RED).score > teams.get(ChatColor.BLUE).score ? teams.get(ChatColor.RED) : teams.get(ChatColor.BLUE);
        if (teams.get(ChatColor.RED).score == teams.get(ChatColor.BLUE).score)
            winnerTeam = null;

        EnumMap<ChatColor, List<GameUser>> scoreRank = new EnumMap<>(ChatColor.class);
        EnumMap<ChatColor, List<GameUser>> damageRank = new EnumMap<>(ChatColor.class);
        EnumMap<ChatColor, List<GameUser>> killRank = new EnumMap<>(ChatColor.class);
        EnumMap<ChatColor, List<GameUser>> defendRank = new EnumMap<>(ChatColor.class);
        EnumMap<ChatColor, List<GameUser>> healRank = new EnumMap<>(ChatColor.class);
        for (Team team : teams.values()) {
            scoreRank.put(team.color, team.teamUsers.stream().sorted(Comparator.comparing(GameUser::getScore).reversed())
                    .collect(Collectors.toList()));
            damageRank.put(team.color, team.teamUsers.stream().sorted(Comparator.comparing(GameUser::getDamage).reversed())
                    .collect(Collectors.toList()));
            killRank.put(team.color, team.teamUsers.stream().sorted(Comparator.comparing(GameUser::getKill).reversed())
                    .collect(Collectors.toList()));
            defendRank.put(team.color, team.teamUsers.stream().sorted(Comparator.comparing(GameUser::getDefend).reversed())
                    .collect(Collectors.toList()));
            healRank.put(team.color, team.teamUsers.stream().sorted(Comparator.comparing(GameUser::getHeal).reversed())
                    .collect(Collectors.toList()));
        }

        for (GameUser gameUser : gameUsers) {
            if (gameUser.getTeam() == null)
                continue;

            gameUser.getUser().clearChat();

            Boolean isWinner = winnerTeam == null ? null : gameUser.getTeam() == winnerTeam;

            int moneyEarned = updateMoney(gameUser, isWinner);
            int xpEarned = updateXp(gameUser, isWinner);
            int rankEarned = 0;

            if (gamePlayMode.isRanked())
                rankEarned = updateRankRate(gameUser, isWinner);
            else
                updateMMR(gameUser);

            UserData userData = gameUser.getUser().getUserData();
            if (Boolean.TRUE.equals(isWinner))
                userData.setWinCount(userData.getWinCount() + 1);
            else if (Boolean.FALSE.equals(isWinner))
                userData.setLoseCount(userData.getLoseCount() + 1);

            sendResultReport(gameUser, isWinner,
                    scoreRank.get(gameUser.getTeam().color).indexOf(gameUser),
                    damageRank.get(gameUser.getTeam().color).indexOf(gameUser),
                    killRank.get(gameUser.getTeam().color).indexOf(gameUser),
                    defendRank.get(gameUser.getTeam().color).indexOf(gameUser),
                    healRank.get(gameUser.getTeam().color).indexOf(gameUser), moneyEarned, xpEarned, rankEarned);
        }
    }

    /**
     * 게임 종료 시 결과 메시지와 효과를 전송한다.
     *
     * @param gameUser    대상 플레이어
     * @param isWinner    승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @param scoreRank   점수 순위
     * @param damageRank  입힌 피해 순위
     * @param killRank    적 처치 순위
     * @param defendRank  막은 피해 순위
     * @param healRank    치유 순위
     * @param moneyEarned 획득한 돈
     * @param xpEarned    획득한 경험치
     * @param rankEarned  획득한 랭크 점수
     */
    private void sendResultReport(@NonNull GameUser gameUser, @Nullable Boolean isWinner, int scoreRank, int damageRank, int killRank, int defendRank,
                                  int healRank, int moneyEarned, int xpEarned, int rankEarned) {
        ChatColor[] rankColors = {ChatColor.YELLOW, ChatColor.WHITE, ChatColor.GOLD, ChatColor.DARK_GRAY, ChatColor.DARK_GRAY, ChatColor.DARK_GRAY};
        ChatColor winColor;
        String winText;
        if (isWinner == null) {
            winColor = ChatColor.YELLOW;
            winText = "무승부";
            playDrawEffect(gameUser);
        } else if (isWinner) {
            winColor = ChatColor.GREEN;
            winText = "승리";
            playWinEffect(gameUser);
        } else {
            winColor = ChatColor.RED;
            winText = "패배";
            playLoseEffect(gameUser);
        }

        gameUser.getUser().sendMessageInfo(
                StringFormUtil.BAR +
                        "\n§d§l플레이 정보 {0}§l[{1}]" +
                        "\n" +
                        "\n{2}§l■ {2}점수 : {3} §l[{4}위]" +
                        "\n{5}§l■ {5}입힌 피해 : {6} §l[{7}위]" +
                        "\n{8}§l■ {8}적 처치 : {9} §l[{10}위]" +
                        "\n{11}§l■ {11}막은 피해 : {12} §l[{13}위]" +
                        "\n{14}§l■ {14}치유 : {15} §l[{16}위]" +
                        "\n§8§l■ §8사망 : {17}" +
                        "\n" +
                        "\n§d§l보상 획득" +
                        "\n" +
                        "\n§e▶ CP 획득 §7:: §6+{18}" +
                        "\n§e▶ 경험치 획득 §7:: §6+{19}" +
                        "\n" +
                        StringFormUtil.BAR,
                winColor, winText,
                rankColors[scoreRank], (int) gameUser.getScore(), scoreRank + 1,
                rankColors[damageRank], gameUser.getDamage(), damageRank + 1,
                rankColors[killRank], gameUser.getKill(), killRank + 1,
                rankColors[defendRank], gameUser.getDefend(), defendRank + 1,
                rankColors[healRank], gameUser.getHeal(), healRank + 1,
                gameUser.getDeath(), moneyEarned, xpEarned);
        if (gamePlayMode.isRanked())
            gameUser.getUser().sendMessageInfo(
                    "\n§d§l랭크" +
                            "\n" +
                            "\n§e▶ 랭크 점수 §7:: {0}{1}" +
                            "\n" + StringFormUtil.BAR,
                    winColor + (rankEarned >= 0 ? "+" : ""), rankEarned);
    }

    /**
     * 승리 시 효과를 재생한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void playWinEffect(@NonNull GameUser gameUser) {
        new DelayTask(() -> {
            gameUser.getUser().sendTitle("§b§l승리", "", 8, 40, 30, 40);
            SoundUtil.playNamedSound(NamedSound.GAME_WIN, gameUser.getPlayer());
        }, 40);
    }

    /**
     * 패배 시 효과를 재생한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void playLoseEffect(@NonNull GameUser gameUser) {
        new DelayTask(() -> {
            gameUser.getUser().sendTitle("§c§l패배", "", 8, 40, 30, 40);
            SoundUtil.playNamedSound(NamedSound.GAME_LOSE, gameUser.getPlayer());
        }, 40);
    }

    /**
     * 무승부 시 효과를 재생한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void playDrawEffect(@NonNull GameUser gameUser) {
        new DelayTask(() -> {
            gameUser.getUser().sendTitle("§e§l무승부", "", 8, 40, 30, 40);
            SoundUtil.playNamedSound(NamedSound.GAME_DRAW, gameUser.getPlayer());
        }, 40);
    }

    /**
     * 매치 종료 후 결과에 따라 플레이어의 경험치를 증가시킨다.
     *
     * @param gameUser 대상 플레이어
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 획득한 경험치
     */
    private int updateXp(@NonNull GameUser gameUser, @Nullable Boolean isWinner) {
        UserData userData = gameUser.getUser().getUserData();

        int xp = userData.getXp();
        double score = gameUser.getScore();

        userData.setXp(GameReward.getFinalXp(xp, score, isWinner));

        return GameReward.getFinalXp(xp, score, isWinner) - xp;
    }

    /**
     * 매치 종료 후 결과에 따라 플레이어의 돈을 증가시킨다.
     *
     * @param gameUser 대상 플레이어
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 획득한 돈
     */
    private int updateMoney(@NonNull GameUser gameUser, @Nullable Boolean isWinner) {
        UserData userData = gameUser.getUser().getUserData();

        int money = userData.getMoney();
        double score = gameUser.getScore();

        userData.setMoney(GameReward.getFinalMoney(money, score, isWinner));

        return userData.getMoney() - money;
    }

    /**
     * 일반 매치 종료 후 결과에 따라 플레이어의 MMR을 조정한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void updateMMR(@NonNull GameUser gameUser) {
        UserData userData = gameUser.getUser().getUserData();

        int mmr = userData.getMatchMakingRate();
        int normalPlayCount = userData.getNormalPlayCount();
        double kda = gameUser.getKDARatio();
        double score = gameUser.getScore();
        int playTime = (int) ((System.currentTimeMillis() - gameUser.getStartTime()) / 1000);
        int gameAverageMMR = (int) gameUser.getGame().getAverageMMR();

        userData.setMatchMakingRate(GameReward.getFinalMMR(mmr, normalPlayCount, kda, score, playTime, gameAverageMMR));
        userData.setNormalPlayCount(normalPlayCount + 1);

        ConsoleLogger.info("{0}의 유저 MMR 변동됨: {1} -> {2}, 일반 매치 플레이 횟수: {3}",
                gameUser.getPlayer().getName(), mmr, userData.getMatchMakingRate(), normalPlayCount + 1);
    }

    /**
     * 랭크 매치 종료 후 결과에 따라 플레이어의 랭크 점수와 MMR을 조정한다.
     *
     * @param gameUser 대상 플레이어
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 랭크 점수 획득량
     */
    private int updateRankRate(@NonNull GameUser gameUser, @Nullable Boolean isWinner) {
        UserData userData = gameUser.getUser().getUserData();

        int mmr = userData.getMatchMakingRate();
        int rr = userData.getRankRate();
        int rankPlayCount = userData.getRankPlayCount();
        double kda = gameUser.getKDARatio();
        double score = gameUser.getScore();
        int playTime = (int) ((System.currentTimeMillis() - gameUser.getStartTime()) / 1000);
        int gameAverageMMR = (int) gameUser.getGame().getAverageMMR();
        int gameAverageRank = (int) gameUser.getGame().getAverageRankRate();

        userData.setMatchMakingRate(GameReward.getFinalMMR(mmr, rankPlayCount, kda, score, playTime, gameAverageMMR));
        userData.setRankPlayCount(rankPlayCount + 1);

        if (!userData.isRanked()) {
            if (rankPlayCount + 1 >= GeneralConfig.getGameConfig().getRankPlacementPlayCount()) {
                userData.setRankRate(GameReward.getFinalRankRate(mmr));
                userData.setRanked(true);
            }
        } else {
            userData.setRankRate(GameReward.getFinalRankRateRanked(mmr, rr, kda, score, playTime, gameAverageRank, isWinner));
            ConsoleLogger.info("{0}의 RR 변동됨: {1} -> {2}, 랭크 매치 플레이 횟수: {3}",
                    gameUser.getPlayer().getName(), rr, userData.getRankRate(), rankPlayCount + 1);
        }

        return userData.getRankRate() - rr;
    }

    /**
     * 플레이어가 게임에 참여할 수 있는 지 확인한다.
     *
     * <p>추가 인원을 수용할 수 있고 게임이 진행 중인 상태이며, 양 팀의 인원수가
     * 다를 때 참여 가능하다.</p>
     *
     * @return 참여 가능 여부
     */
    public boolean canJoin() {
        if (gameUsers.size() >= maxPlayerCount)
            return false;

        int redAmount = teams.get(ChatColor.RED).teamUsers.size();
        int blueAmount = teams.get(ChatColor.BLUE).teamUsers.size();

        if (phase != Phase.WAITING)
            return !gamePlayMode.isRanked() && redAmount != blueAmount;

        return true;
    }

    /**
     * 게임에 지정한 플레이어를 추가한다.
     *
     * <p>게임이 진행 중이면 인원이 부족한 팀이 있을 때만 난입이 가능하다.</p>
     *
     * @param gameUser 대상 플레이어
     */
    void addPlayer(@NonNull GameUser gameUser) {
        validate();

        if (!canJoin())
            return;

        int redAmount = teams.get(ChatColor.RED).teamUsers.size();
        int blueAmount = teams.get(ChatColor.BLUE).teamUsers.size();

        gameUsers.add(gameUser);
        gameUsers.forEach(target -> target.getUser().sendMessageInfo(StringFormUtil.ADD_PREFIX + gameUser.getPlayer().getName()));

        if (phase != Phase.WAITING) {
            Team team = redAmount < blueAmount ? teams.get(ChatColor.RED) : teams.get(ChatColor.BLUE);

            gameUser.setTeam(team);
            team.teamUsers.add(gameUser);
        }
    }

    /**
     * 게임에서 지정한 플레이어를 제거한다.
     *
     * @param gameUser 대상 플레이어
     */
    void removePlayer(@NonNull GameUser gameUser) {
        validate();

        gameUser.getUser().clearBossBar();

        gameUsers.forEach(target -> target.getUser().sendMessageInfo(StringFormUtil.REMOVE_PREFIX + gameUser.getPlayer().getName()));
        gameUsers.remove(gameUser);
        if (gameUser.getTeam() != null)
            teams.get(gameUser.getTeam().color).teamUsers.remove(gameUser);

        if (phase != Phase.END && gameUsers.isEmpty())
            phase = Phase.END;
    }

    /**
     * 게임에 참여한 모든 플레이어의 MMR 평균을 반환한다.
     *
     * @return 참여자들의 MMR 평균
     */
    private double getAverageMMR() {
        return gameUsers.stream()
                .mapToInt(gameUser -> gameUser.getUser().getUserData().getMatchMakingRate())
                .average()
                .orElse(0);
    }

    /**
     * 게임에 참여한 모든 플레이어의 랭크 점수 평균을 반환한다.
     *
     * @return 참여자들의 랭크 점수 평균
     */
    private double getAverageRankRate() {
        return gameUsers.stream()
                .mapToInt(gameUser -> gameUser.getUser().getUserData().getRankRate())
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

        @NonNull
        private final String name;
    }

    /**
     * 게임에서 사용하는 팀 정보를 관리하는 클래스.
     */
    @RequiredArgsConstructor
    public static final class Team {
        /** 팀 색 */
        @NonNull
        @Getter
        private final ChatColor color;
        /** 팀 이름 */
        @NonNull
        @Getter
        private final String name;
        /** 소속된 플레이어 목록 */
        private final ArrayList<GameUser> teamUsers = new ArrayList<>();
        /** 팀 점수 */
        @Getter
        @Setter
        private int score;

        @NonNull
        public GameUser @NonNull [] getTeamUsers() {
            return teamUsers.toArray(new GameUser[0]);
        }
    }
}
