package com.dace.dmgr.game;

import com.dace.dmgr.*;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.effect.BossBarDisplay;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.*;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;

/**
 * 게임의 정보와 진행을 관리하는 클래스.
 */
public final class Game implements Disposable {
    /** 타이머 효과음 */
    private static final SoundEffect TIMER_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(1000).pitch(1).build());
    /** 전투 시작 효과음 */
    private static final SoundEffect ON_PLAY_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_WITHER_SPAWN).volume(1000).pitch(1).build());
    /** 승리 효과음 */
    private static final SoundEffect WIN_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.UI_TOAST_CHALLENGE_COMPLETE).volume(1000).pitch(1.5).build());
    /** 패배 효과음 */
    private static final SoundEffect LOSE_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_BLAZE_DEATH).volume(1000).pitch(0.5).build());
    /** 무승부 효과음 */
    private static final SoundEffect DRAW_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_LEVELUP).volume(1000).pitch(1).build());
    /** 게임 시작 대기 보스바 목록 */
    private final BossBarDisplay[] gameWaitBossBars = new BossBarDisplay[]{
            new BossBarDisplay("§f대기열에서 나가려면 §n'/quit'§f 또는 §n'/q'§f를 입력하십시오."),
            new BossBarDisplay(""),
            new BossBarDisplay("", BarColor.GREEN, BarStyle.SOLID)
    };
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
    /** 레드 팀 */
    @NonNull
    @Getter
    private final Team redTeam;
    /** 블루 팀 */
    @NonNull
    @Getter
    private final Team blueTeam;
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
        redTeam = new Team(ChatColor.RED, "레드");
        blueTeam = new Team(ChatColor.BLUE, "블루");
        GameRegistry.getInstance().add(new GameRegistry.KeyPair(isRanked, number), this);

        TaskUtil.addTask(this, new IntervalTask((LongConsumer) i -> onSecond(), 20));
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
    @Unmodifiable
    public Collection<@NonNull CombatEntity> getAllCombatEntities() {
        return combatEntities;
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
    @Unmodifiable
    public Collection<@NonNull GameUser> getGameUsers() {
        return gameUsers;
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
        gameWaitBossBars[1].setTitle(MessageFormat.format("§c게임을 시작하려면 최소 {0}명이 필요합니다.", minPlayerCount));
        gameWaitBossBars[2].setTitle(MessageFormat.format("§a§l{0} §f[{1}§f/{2} 명]",
                (gamePlayMode.isRanked() ? "§6§l랭크" : "§a§l일반"),
                (canStart() ? "§f" : "§c") + gameUsers.size(),
                maxPlayerCount));
        gameWaitBossBars[2].setProgress(canStart() ? (double) remainingTime / GeneralConfig.getGameConfig().getWaitingTimeSeconds() : 1);

        for (BossBarDisplay gameWaitBossBar : gameWaitBossBars)
            gameWaitBossBar.show(gameUser.getPlayer());
    }

    /**
     * 진행 단계가 {@link Phase#READY}일 때 매 초마다 실행할 작업.
     */
    private void onSecondReady() {
        if (remainingTime > 0 && remainingTime <= 5) {
            gameUsers.forEach(gameUser -> {
                TIMER_SOUND.play(gameUser.getPlayer());
                gameUser.getUser().sendTitle("§f" + remainingTime, "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10), Timespan.ofTicks(10));
            });
        }

        if (remainingTime == 0) {
            phase = Phase.PLAYING;
            remainingTime = gamePlayMode.getPlayDuration();

            gameUsers.forEach(gameUser -> {
                ON_PLAY_SOUND.play(gameUser.getPlayer());
                gameUser.getUser().sendTitle("§c§l전투 시작", "", Timespan.ZERO, Timespan.ofTicks(40), Timespan.ofTicks(20), Timespan.ofTicks(40));
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

        if (remainingTime > 0) {
            int ultPackRemainingTime = remainingTime - (gamePlayMode.getPlayDuration() - GeneralConfig.getGameConfig().getUltPackActivationSeconds());

            if (ultPackRemainingTime >= 0 && ultPackRemainingTime <= 20) {
                if (ultPackRemainingTime == 5 || ultPackRemainingTime == 10 || ultPackRemainingTime == 20)
                    gameUsers.forEach(gameUser -> {
                        TIMER_SOUND.play(gameUser.getPlayer());
                        gameUser.getUser().sendTitle("", "§9궁극기 팩§f이 §e" + ultPackRemainingTime + "초 §f후 활성화됩니다.",
                                Timespan.ZERO, Timespan.ofTicks(20), Timespan.ofTicks(20), Timespan.ofTicks(40));
                    });
                else if (ultPackRemainingTime == 0)
                    gameUsers.forEach(gameUser -> {
                        TIMER_SOUND.play(gameUser.getPlayer());
                        gameUser.getUser().sendTitle("", "§9궁극기 팩§f이 활성화되었습니다.", Timespan.ZERO, Timespan.ofTicks(20),
                                Timespan.ofTicks(20), Timespan.ofTicks(40));
                    });
            }

            if (remainingTime <= 10) {
                gameUsers.forEach(gameUser -> {
                    TIMER_SOUND.play(gameUser.getPlayer());
                    gameUser.getUser().sendTitle("", "§c" + remainingTime, Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10),
                            Timespan.ofTicks(10));
                });
            }
        }

        if (remainingTime == 0) {
            phase = Phase.END;
            onFinish();
        }
    }

    /**
     * 게임을 시작할 조건이 충족되었는지 확인한다.
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
                for (BossBarDisplay gameWaitBossBar : gameWaitBossBars)
                    gameWaitBossBar.hide(gameUser.getPlayer());

                gameUser.onGameStart();
            });
        }).onError(ex -> {
            gameUsers.forEach(gameUser -> gameUser.getUser()
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
        HashSet<GameUser> team1users = new HashSet<>();
        HashSet<GameUser> team2users = new HashSet<>();
        int size = sortedGameUsers.size();

        for (int i = 0; i < size / 4; i++)
            team1users.add(sortedGameUsers.pop());
        for (int i = 0; i < size / 2; i++)
            team2users.add(sortedGameUsers.pop());
        team1users.addAll(sortedGameUsers);

        Team team1Team = redTeam;
        Team team2Team = blueTeam;
        if (DMGR.getRandom().nextBoolean()) {
            team1Team = blueTeam;
            team2Team = redTeam;
        }

        for (GameUser gameUser : team1users) {
            gameUser.setTeam(team1Team);
            team1Team.teamUsers.add(gameUser);
        }
        for (GameUser gameUser : team2users) {
            gameUser.setTeam(team2Team);
            team2Team.teamUsers.add(gameUser);
        }
    }

    /**
     * 게임 종료 시 실행할 작업.
     */
    private void onFinish() {
        Team winnerTeam = redTeam.score > blueTeam.score ? redTeam : blueTeam;
        if (redTeam.score == blueTeam.score)
            winnerTeam = null;

        HashMap<Team, List<GameUser>> scoreRank = new HashMap<>();
        HashMap<Team, List<GameUser>> damageRank = new HashMap<>();
        HashMap<Team, List<GameUser>> killRank = new HashMap<>();
        HashMap<Team, List<GameUser>> defendRank = new HashMap<>();
        HashMap<Team, List<GameUser>> healRank = new HashMap<>();
        for (Team team : new Team[]{redTeam, blueTeam}) {
            scoreRank.put(team, team.teamUsers.stream().sorted(Comparator.comparing(GameUser::getScore).reversed())
                    .collect(Collectors.toList()));
            damageRank.put(team, team.teamUsers.stream().sorted(Comparator.comparing(GameUser::getDamage).reversed())
                    .collect(Collectors.toList()));
            killRank.put(team, team.teamUsers.stream().sorted(Comparator.comparing(GameUser::getKill).reversed())
                    .collect(Collectors.toList()));
            defendRank.put(team, team.teamUsers.stream().sorted(Comparator.comparing(GameUser::getDefend).reversed())
                    .collect(Collectors.toList()));
            healRank.put(team, team.teamUsers.stream().sorted(Comparator.comparing(GameUser::getHeal).reversed())
                    .collect(Collectors.toList()));
        }

        for (GameUser gameUser : gameUsers) {
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
                userData.addWinCount();
            else if (Boolean.FALSE.equals(isWinner))
                userData.addLoseCount();

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
                rankColors[damageRank], (int) gameUser.getDamage(), damageRank + 1,
                rankColors[killRank], gameUser.getKill(), killRank + 1,
                rankColors[defendRank], (int) gameUser.getDefend(), defendRank + 1,
                rankColors[healRank], (int) gameUser.getHeal(), healRank + 1,
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
            gameUser.getUser().sendTitle("§b§l승리", "", Timespan.ofSeconds(0.4), Timespan.ofSeconds(2), Timespan.ofSeconds(1.5),
                    Timespan.ofSeconds(2));
            WIN_SOUND.play(gameUser.getPlayer());
        }, 40);
    }

    /**
     * 패배 시 효과를 재생한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void playLoseEffect(@NonNull GameUser gameUser) {
        new DelayTask(() -> {
            gameUser.getUser().sendTitle("§c§l패배", "", Timespan.ofSeconds(0.4), Timespan.ofSeconds(2), Timespan.ofSeconds(1.5),
                    Timespan.ofSeconds(2));
            LOSE_SOUND.play(gameUser.getPlayer());
        }, 40);
    }

    /**
     * 무승부 시 효과를 재생한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void playDrawEffect(@NonNull GameUser gameUser) {
        new DelayTask(() -> {
            gameUser.getUser().sendTitle("§e§l무승부", "", Timespan.ofSeconds(0.4), Timespan.ofSeconds(2), Timespan.ofSeconds(1.5),
                    Timespan.ofSeconds(2));
            DRAW_SOUND.play(gameUser.getPlayer());
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
        userData.addNormalPlayCount();

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
        userData.addRankPlayCount();

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
     * 플레이어가 게임에 참여할 수 있는지 확인한다.
     *
     * <p>추가 인원을 수용할 수 있고 게임이 진행 중인 상태이며, 양 팀의 인원수가
     * 다를 때 참여 가능하다.</p>
     *
     * @return 참여 가능 여부
     */
    public boolean canJoin() {
        if (gameUsers.size() >= maxPlayerCount)
            return false;

        if (phase != Phase.WAITING)
            return !gamePlayMode.isRanked() && redTeam.teamUsers.size() != blueTeam.teamUsers.size();

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

        gameUsers.add(gameUser);
        gameUsers.forEach(target -> target.getUser().sendMessageInfo(StringFormUtil.ADD_PREFIX + gameUser.getPlayer().getName()));

        if (phase != Phase.WAITING) {
            Team team = redTeam.teamUsers.size() < blueTeam.teamUsers.size() ? redTeam : blueTeam;

            team.teamUsers.add(gameUser);
            gameUser.setTeam(team);
            gameUser.onGameStart();
        }
    }

    /**
     * 게임에서 지정한 플레이어를 제거한다.
     *
     * @param gameUser 대상 플레이어
     */
    void removePlayer(@NonNull GameUser gameUser) {
        validate();

        for (BossBarDisplay gameWaitBossBar : gameWaitBossBars)
            gameWaitBossBar.hide(gameUser.getPlayer());

        gameUsers.forEach(target -> target.getUser().sendMessageInfo(StringFormUtil.REMOVE_PREFIX + gameUser.getPlayer().getName()));
        gameUsers.remove(gameUser);
        if (gameUser.getTeam() != null)
            gameUser.getTeam().teamUsers.remove(gameUser);

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
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
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
        @Unmodifiable
        public Collection<@NonNull GameUser> getTeamUsers() {
            return teamUsers;
        }
    }
}
