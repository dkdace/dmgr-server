package com.dace.dmgr.game;

import com.comphenix.packetwrapper.WrapperPlayServerBoss;
import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.Disposable;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.game.map.GlobalLocation;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.boss.BarColor;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 게임의 정보와 진행을 관리하는 클래스.
 */
public final class Game implements Disposable {
    /** 방 번호 */
    @Getter
    private final int number;
    /** 소속된 엔티티 목록 */
    private final HashSet<CombatEntity> combatEntitySet = new HashSet<>();
    /** 소속된 플레이어 목록 */
    @NonNull
    @Getter
    private final ArrayList<GameUser> gameUsers = new ArrayList<>();
    /** 게임을 시작하기 위한 최소 인원 수 */
    private final int minPlayerCount;
    /** 최대 수용 가능 인원 수 */
    private final int maxPlayerCount;
    /** 팀별 플레이어 목록 (팀 : 플레이어 목록) */
    @Getter
    private final EnumMap<@NonNull Team, @NonNull ArrayList<GameUser>> teamUserMap = new EnumMap<>(Team.class);
    /** 팀별 점수 (팀 : 점수) */
    @Getter
    private final EnumMap<@NonNull Team, Integer> teamScore = new EnumMap<>(Team.class);
    /** 게임 모드 */
    @NonNull
    @Getter
    private final GamePlayMode gamePlayMode;
    /** 맵 */
    @NonNull
    @Getter
    private final GameMap map;
    /** 전장 월드 */
    @Getter
    private World world;
    /** 게임 시작 시점 ({@link Phase#READY}가 된 시점) */
    @Getter
    private long startTime = 0;
    /** 다음 진행 단계까지 남은 시간 (초) */
    @Getter
    private int remainingTime = GeneralConfig.getGameConfig().getWaitingTime();
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
        Game game = GameRegistry.getInstance().get(new KeyPair(isRanked, number));
        if (game != null)
            throw new IllegalStateException(MessageFormat.format("랭크 여부 {0}, 방 번호 {1}의 Game이 이미 생성됨", isRanked, number));

        this.number = number;
        this.gamePlayMode = GameUtil.getRandomGamePlayMode(isRanked);
        this.map = GameUtil.getRandomMap(gamePlayMode);
        this.world = Bukkit.getWorld(MessageFormat.format("_{0}-{1}-{2}", map.getWorld().getName(), gamePlayMode, number));
        minPlayerCount = isRanked ? GeneralConfig.getGameConfig().getRankMinPlayerCount() : GeneralConfig.getGameConfig().getNormalMinPlayerCount();
        maxPlayerCount = isRanked ? GeneralConfig.getGameConfig().getRankMaxPlayerCount() : GeneralConfig.getGameConfig().getNormalMaxPlayerCount();
        for (Team team : Team.values()) {
            this.teamUserMap.put(team, new ArrayList<>());
            this.teamScore.put(team, 0);
        }
        GameRegistry.getInstance().add(new KeyPair(isRanked, number), this);

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
    public static Game fromNumber(boolean isRanked, int number) {
        return GameRegistry.getInstance().get(new KeyPair(isRanked, number));
    }

    @Override
    public void dispose() {
        checkAccess();

        if (!gameUsers.isEmpty())
            for (GameUser gameUser : new ArrayList<>(gameUsers)) {
                if (phase != Phase.WAITING)
                    gameUser.getUser().reset();

                gameUser.dispose();
            }

        for (GlobalLocation healPackLocation : map.getHealPackLocations())
            HologramUtil.removeHologram("healpack" + healPackLocation);

        TaskUtil.clearTask(this);
        removeWorld().onFinish(() -> {
            world = null;
            GameRegistry.getInstance().remove(new KeyPair(gamePlayMode.isRanked(), number));
        });
    }

    @Override
    public boolean isDisposed() {
        return GameRegistry.getInstance().get(new KeyPair(gamePlayMode.isRanked(), number)) == null;
    }

    /**
     * 게임에 소속된 모든 엔티티 목록을 반환한다.
     *
     * @return 게임에 속한 모든 엔티티
     */
    @NonNull
    public CombatEntity[] getAllCombatEntities() {
        return combatEntitySet.toArray(new CombatEntity[0]);
    }

    /**
     * 게임에 지정한 엔티티를 추가한다.
     *
     * @param combatEntity 전투 시스템의 엔티티 객체
     */
    public void addCombatEntity(@NonNull CombatEntity combatEntity) {
        combatEntitySet.add(combatEntity);
    }

    /**
     * 게임에 소속된 지정한 엔티티를 제거한다.
     *
     * @param combatEntity 전투 시스템의 엔티티 객체
     */
    public void removeCombatEntity(@NonNull CombatEntity combatEntity) {
        combatEntitySet.remove(combatEntity);
    }

    /**
     * 전장으로 사용할 월드를 생성한다.
     */
    @NonNull
    private AsyncTask<World> createWorld() {
        if (world == null)
            return WorldUtil.duplicateWorld(map.getWorld(), MessageFormat.format("_{0}-{1}-{2}",
                    map.getWorld().getName(), gamePlayMode, number));

        return new AsyncTask<>((onFinish, onError) -> onFinish.accept(world));
    }

    /**
     * 전장 월드를 제거한다.
     */
    @NonNull
    private AsyncTask<Void> removeWorld() {
        if (world != null)
            return WorldUtil.removeWorld(world.getName());

        return new AsyncTask<>((onFinish, onError) -> onFinish.accept(null));
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

        if (canStart()) {
            if (remainingTime > 0 && (remainingTime <= 5 || remainingTime == 10))
                gameUsers.forEach(gameUser -> gameUser.getUser()
                        .sendMessageInfo("게임이 {0}초 뒤에 시작합니다.", remainingTime));

            if (remainingTime == 0) {
                phase = Phase.READY;

                onReady();
            }
        } else
            remainingTime = GeneralConfig.getGameConfig().getWaitingTime();
    }

    /**
     * 플레이어에게 게임 시작 대기 보스바를 전송한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void sendBossBarWaiting(@NonNull GameUser gameUser) {
        gameUser.getUser().addBossBar("GameWait1", "§f대기열에서 나가려면 §n'/quit'§f 또는 §n'/q'§f를 입력하십시오.", BarColor.WHITE,
                WrapperPlayServerBoss.BarStyle.PROGRESS, 0);
        gameUser.getUser().addBossBar("GameWait2",
                MessageFormat.format("§c게임을 시작하려면 최소 {0}명이 필요합니다.", minPlayerCount),
                BarColor.WHITE, WrapperPlayServerBoss.BarStyle.PROGRESS, 0);
        gameUser.getUser().addBossBar("GameWait3",
                MessageFormat.format("§a§l{0} §f[{1}§f/{2} 명]",
                        (gamePlayMode.isRanked() ? "§6§l랭크" : "§a§l일반"), (canStart() ? "§f" : "§c") + gameUsers.size(),
                        maxPlayerCount),
                BarColor.GREEN,
                WrapperPlayServerBoss.BarStyle.PROGRESS,
                canStart() ? (double) remainingTime / GeneralConfig.getGameConfig().getWaitingTime() : 1);
    }

    /**
     * 진행 단계가 {@link Phase#READY}일 때 매 초마다 실행할 작업.
     */
    private void onSecondReady() {
        if (remainingTime > 0 && remainingTime <= 5) {
            gameUsers.forEach(gameUser -> {
                SoundUtil.play(NamedSound.GAME_TIMER, gameUser.getPlayer());
                gameUser.getUser().sendTitle("§f" + remainingTime, "", 0, 5, 10, 10);
            });
        }

        if (remainingTime == 0) {
            phase = Phase.PLAYING;
            remainingTime = gamePlayMode.getPlayDuration();

            gameUsers.forEach(gameUser -> {
                SoundUtil.play(NamedSound.GAME_ON_PLAY, gameUser.getPlayer());
                gameUser.getUser().sendTitle("§c§l전투 시작", "", 0, 40, 20, 40);
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
                SoundUtil.play(NamedSound.GAME_TIMER, gameUser.getPlayer());
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

        createWorld().onFinish(newWorld -> {
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
     * <li>1팀, 2팀을 무작위로 {@link Team#RED} 또는 {@link Team#BLUE}로 지정한다.</li>
     * </ol>
     */
    private void divideTeam() {
        LinkedList<GameUser> sortedGameUsers = gameUsers.stream()
                .sorted(Comparator.comparing(gameUser ->
                        UserData.fromPlayer(((GameUser) gameUser).getPlayer()).getMatchMakingRate()).reversed())
                .collect(Collectors.toCollection(LinkedList::new));
        HashSet<GameUser> team1 = new HashSet<>();
        HashSet<GameUser> team2 = new HashSet<>();
        int size = sortedGameUsers.size();

        for (int i = 0; i < size / 4; i++)
            team1.add(sortedGameUsers.pop());
        for (int i = 0; i < size / 2; i++)
            team2.add(sortedGameUsers.pop());
        team1.addAll(sortedGameUsers);

        Team team1Team = Team.RED;
        Team team2Team = Team.BLUE;
        if (DMGR.getRandom().nextBoolean()) {
            team1Team = Team.BLUE;
            team2Team = Team.RED;
        }

        for (GameUser gameUser : team1) {
            gameUser.setTeam(team1Team);
            teamUserMap.get(team1Team).add(gameUser);
        }
        for (GameUser gameUser : team2) {
            gameUser.setTeam(team2Team);
            teamUserMap.get(team2Team).add(gameUser);
        }
    }

    /**
     * 게임 종료 시 실행할 작업.
     */
    private void onFinish() {
        Team winnerTeam = teamScore.get(Team.RED) > teamScore.get(Team.BLUE) ? Team.RED : Team.BLUE;
        if (teamScore.get(Team.RED).equals(teamScore.get(Team.BLUE)))
            winnerTeam = Team.NONE;

        EnumMap<Team, List<GameUser>> scoreRank = new EnumMap<>(Team.class);
        EnumMap<Team, List<GameUser>> damageRank = new EnumMap<>(Team.class);
        EnumMap<Team, List<GameUser>> killRank = new EnumMap<>(Team.class);
        EnumMap<Team, List<GameUser>> defendRank = new EnumMap<>(Team.class);
        EnumMap<Team, List<GameUser>> healRank = new EnumMap<>(Team.class);
        for (Team team : Team.values()) {
            if (team == Team.NONE)
                continue;

            scoreRank.put(team, teamUserMap.get(team).stream().sorted(Comparator.comparing(GameUser::getScore).reversed())
                    .collect(Collectors.toList()));
            damageRank.put(team, teamUserMap.get(team).stream().sorted(Comparator.comparing(GameUser::getDamage).reversed())
                    .collect(Collectors.toList()));
            killRank.put(team, teamUserMap.get(team).stream().sorted(Comparator.comparing(GameUser::getKill).reversed())
                    .collect(Collectors.toList()));
            defendRank.put(team, teamUserMap.get(team).stream().sorted(Comparator.comparing(GameUser::getDefend).reversed())
                    .collect(Collectors.toList()));
            healRank.put(team, teamUserMap.get(team).stream().sorted(Comparator.comparing(GameUser::getHeal).reversed())
                    .collect(Collectors.toList()));
        }

        for (GameUser gameUser : gameUsers) {
            gameUser.getUser().clearChat();

            Boolean isWinner = winnerTeam == Team.NONE ? null : gameUser.getTeam() == winnerTeam;

            int moneyEarned = updateMoney(gameUser, isWinner);
            int xpEarned = updateXp(gameUser, isWinner);
            int rankEarned = 0;

            if (gamePlayMode.isRanked())
                rankEarned = updateRankRate(gameUser, isWinner);
            else
                updateMMR(gameUser);

            sendResultReport(gameUser, isWinner,
                    scoreRank.get(gameUser.getTeam()).indexOf(gameUser),
                    damageRank.get(gameUser.getTeam()).indexOf(gameUser),
                    killRank.get(gameUser.getTeam()).indexOf(gameUser),
                    defendRank.get(gameUser.getTeam()).indexOf(gameUser),
                    healRank.get(gameUser.getTeam()).indexOf(gameUser), moneyEarned, xpEarned, rankEarned);
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
    private void sendResultReport(@NonNull GameUser gameUser, Boolean isWinner, int scoreRank, int damageRank, int killRank, int defendRank, int healRank,
                                  int moneyEarned, int xpEarned, int rankEarned) {
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
                "§7==============================" +
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
                        "\n§7==============================",
                winColor, winText,
                rankColors[scoreRank], gameUser.getScore(), scoreRank + 1,
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
                            "\n§7==============================",
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
            SoundUtil.play(NamedSound.GAME_WIN, gameUser.getPlayer());
        }, 40).run();
    }

    /**
     * 패배 시 효과를 재생한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void playLoseEffect(@NonNull GameUser gameUser) {
        new DelayTask(() -> {
            gameUser.getUser().sendTitle("§c§l패배", "", 8, 40, 30, 40);
            SoundUtil.play(NamedSound.GAME_LOSE, gameUser.getPlayer());
        }, 40).run();
    }

    /**
     * 무승부 시 효과를 재생한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void playDrawEffect(@NonNull GameUser gameUser) {
        new DelayTask(() -> {
            gameUser.getUser().sendTitle("§e§l무승부", "", 8, 40, 30, 40);
            SoundUtil.play(NamedSound.GAME_DRAW, gameUser.getPlayer());
        }, 40).run();
    }

    /**
     * 매치 종료 후 결과에 따라 플레이어의 경험치를 증가시킨다.
     *
     * @param gameUser 대상 플레이어
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 획득한 경험치
     */
    private int updateXp(@NonNull GameUser gameUser, Boolean isWinner) {
        UserData userData = UserData.fromPlayer(gameUser.getPlayer());

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
    private int updateMoney(@NonNull GameUser gameUser, Boolean isWinner) {
        UserData userData = UserData.fromPlayer(gameUser.getPlayer());

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
        UserData userData = UserData.fromPlayer(gameUser.getPlayer());

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
    private int updateRankRate(@NonNull GameUser gameUser, Boolean isWinner) {
        UserData userData = UserData.fromPlayer(gameUser.getPlayer());

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
     * @param gameUser 대상 플레이어
     */
    void addPlayer(@NonNull GameUser gameUser) {
        if (!canJoin())
            return;

        int redAmount = teamUserMap.get(Team.RED).size();
        int blueAmount = teamUserMap.get(Team.BLUE).size();

        gameUsers.add(gameUser);
        gameUsers.forEach(gameUser2 -> gameUser2.getUser().sendMessageInfo(StringFormUtil.ADD_PREFIX + gameUser.getPlayer().getName()));

        if (phase != Phase.WAITING) {
            if (redAmount < blueAmount)
                gameUser.setTeam(Team.RED);
            else
                gameUser.setTeam(Team.BLUE);

            teamUserMap.get(gameUser.getTeam()).add(gameUser);
            gameUser.onGameStart();
        }
    }

    /**
     * 게임에서 지정한 플레이어를 제거한다.
     *
     * @param gameUser 대상 플레이어
     */
    void removePlayer(@NonNull GameUser gameUser) {
        gameUser.getUser().clearBossBar();

        gameUsers.forEach(gameUser2 -> gameUser2.getUser().sendMessageInfo(StringFormUtil.REMOVE_PREFIX + gameUser.getPlayer().getName()));
        gameUsers.remove(gameUser);
        teamUserMap.get(gameUser.getTeam()).remove(gameUser);

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
                .mapToInt(gameUser -> UserData.fromPlayer(gameUser.getPlayer()).getMatchMakingRate())
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
                .mapToInt(gameUser -> UserData.fromPlayer(gameUser.getPlayer()).getRankRate())
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
     * {@link GameRegistry}에서 키 값으로 사용되는 클래스.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode
    public static final class KeyPair {
        /** 랭크 여부 */
        private final boolean isRanked;
        /** 방 번호 */
        private final int number;
    }
}
