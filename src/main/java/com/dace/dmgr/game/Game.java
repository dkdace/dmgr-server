package com.dace.dmgr.game;

import com.dace.dmgr.*;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.effect.BossBarDisplay;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.game.mode.GamePlayMode;
import com.dace.dmgr.game.mode.GamePlayModeScheduler;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.Initializable;
import com.dace.dmgr.util.task.IntervalTask;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.commands.CommandManager;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;
import lombok.*;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * 게임의 내부 진행 시스템을 관리하는 클래스.
 */
public final class Game implements Initializable<Void>, Disposable {
    /** 타이머 효과음 */
    private static final SoundEffect TIMER_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(1000).pitch(1).build());
    /** 전투 시작 효과음 */
    private static final SoundEffect ON_PLAY_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_WITHER_SPAWN).volume(1000).pitch(1).build());

    /** 현재 게임 방 인스턴스 */
    private final GameRoom gameRoom;
    /** 소속된 엔티티 목록 */
    private final HashSet<CombatEntity> combatEntities = new HashSet<>();
    /** 소속된 게임 플레이어 목록 */
    private final HashSet<GameUser> gameUsers = new HashSet<>();
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
    private final GameMap gameMap;
    /** 게임 모드 스케쥴러 */
    private final GamePlayModeScheduler gamePlayModeScheduler;

    /** 초기화 여부 */
    @Getter
    private boolean isInitialized = false;
    /** 비활성화 여부 */
    @Getter
    private boolean isDisposed = false;

    /** 게임 진행 시점 */
    private Timestamp playTimestamp = Timestamp.now();
    /** 게임 종료 시점 */
    private Timestamp endTimestamp = Timestamp.now();
    /** 게임 준비 및 진행 작업을 처리하는 태스크 */
    @Nullable
    private IntervalTask onSecondTask;
    /** 전장 월드 */
    @Nullable
    private World world;
    /** 게임 진행 중 여부 */
    @Getter
    private boolean isPlaying = false;

    /**
     * 게임 진행 시스템 인스턴스를 생성한다.
     *
     * @param gameRoom 대상 게임 방 인스턴스
     */
    Game(@NonNull GameRoom gameRoom) {
        this.gameRoom = gameRoom;
        this.gamePlayMode = GamePlayMode.getRandomGamePlayMode(gameRoom.isRanked());
        this.gameMap = gamePlayMode.getRandomMap();
        this.redTeam = new Team(this, Team.Type.RED);
        this.blueTeam = new Team(this, Team.Type.BLUE);
        this.gamePlayModeScheduler = gamePlayMode.createScheduler(this);
    }

    @Override
    @NonNull
    public AsyncTask<Void> init() {
        if (isInitialized)
            throw new IllegalStateException("인스턴스가 이미 초기화됨");

        gameRoom.getUsers().forEach(user -> user.sendMessageInfo("월드 불러오는 중..."));

        return duplicateWorld()
                .onFinish(() -> {
                    if (!gameRoom.canStart()) {
                        dispose();
                        return;
                    }

                    isInitialized = true;
                    initTeamUsers();

                    int readySeconds = (int) gamePlayMode.getReadyDuration().toSeconds();
                    this.onSecondTask = new IntervalTask(i -> onSecondReady((int) (readySeconds - i)), this::onPlay, 20,
                            readySeconds + 1L);
                })
                .onError(ex -> {
                    gameRoom.getUsers().forEach(user -> user.sendMessageWarn("오류로 인해 월드를 불러올 수 없습니다. 관리자에게 문의하십시오."));
                    dispose();
                });
    }

    /**
     * 게임 시스템을 종료하고 게임 방을 제거한다.
     */
    @Override
    public void dispose() {
        if (isDisposed)
            throw new IllegalStateException("인스턴스가 이미 폐기됨");

        if (isInitialized())
            validate();

        if (!gameUsers.isEmpty()) {
            new ResultManager(this);
            new ArrayList<>(gameUsers).forEach(GameUser::dispose);
        }

        isPlaying = false;
        isDisposed = true;
        gameRoom.dispose();

        if (onSecondTask != null)
            onSecondTask.dispose();
        if (world != null)
            removeWorld();
    }

    /**
     * 게임에 소속된 모든 엔티티 목록을 반환한다.
     *
     * @return 게임에 속한 모든 엔티티
     */
    @NonNull
    @UnmodifiableView
    public Set<@NonNull CombatEntity> getCombatEntities() {
        return Collections.unmodifiableSet(combatEntities);
    }

    /**
     * 게임에 지정한 엔티티를 추가한다.
     *
     * @param combatEntity 전투 시스템의 엔티티 인스턴스
     */
    public void addCombatEntity(@NonNull CombatEntity combatEntity) {
        combatEntities.add(combatEntity);
    }

    /**
     * 게임에 소속된 지정한 엔티티를 제거한다.
     *
     * @param combatEntity 전투 시스템의 엔티티 인스턴스
     */
    public void removeCombatEntity(@NonNull CombatEntity combatEntity) {
        combatEntities.remove(combatEntity);
    }

    /**
     * 게임에 소속된 모든 게임 플레이어 목록을 반환한다.
     *
     * @return 게임에 속한 모든 게임 플레이어
     */
    @NonNull
    @UnmodifiableView
    public Set<@NonNull GameUser> getGameUsers() {
        return Collections.unmodifiableSet(gameUsers);
    }

    /**
     * 게임의 모든 참여자에게 표시되는 전체 보스바를 추가한다.
     *
     * @param bossBarDisplay 추가할 보스바
     * @see GameRoom#addBossBar(BossBarDisplay)
     */
    public void addBossBar(@NonNull BossBarDisplay bossBarDisplay) {
        gameRoom.addBossBar(bossBarDisplay);
    }

    /**
     * 게임의 모든 참여자에게 표시되는 전체 보스바를 제거한다.
     *
     * @param bossBarDisplay 제거할 보스바
     * @see GameRoom#removeBossBar(BossBarDisplay)
     */
    public void removeBossBar(@NonNull BossBarDisplay bossBarDisplay) {
        gameRoom.removeBossBar(bossBarDisplay);
    }

    /**
     * 게임 시작 후 경과한 시간을 반환한다.
     *
     * @return 경과 시간
     */
    @NonNull
    public Timespan getElapsedTime() {
        if (isPlaying())
            return playTimestamp.until(Timestamp.now());

        return Timespan.ZERO;
    }

    /**
     * 게임 종료까지 남은 시간을 반환한다.
     *
     * @return 종료까지 남은 시간
     */
    @NonNull
    public Timespan getRemainingTime() {
        if (isDisposed)
            return Timespan.ZERO;

        if (isPlaying())
            return Timestamp.now().until(endTimestamp);
        else
            return gamePlayMode.getPlayDuration();
    }

    /**
     * 궁극기 팩이 활성화 되었는지 확인한다.
     *
     * @return 궁극기 팩 활성화 여부
     */
    public boolean isUltPackActivated() {
        if (isPlaying())
            return Timestamp.now().isAfter(playTimestamp.plus(GeneralConfig.getGameConfig().getUltPackActivationTime()));

        return false;
    }

    /**
     * 게임 진행을 위한 새로운 복제본 월드를 생성한다.
     */
    @NonNull
    private AsyncTask<Void> duplicateWorld() {
        String worldName = gameMap.getWorld().getName();
        String targetWorldName = MessageFormat.format("{0}{1}-{2}-{3}",
                DMGR.TEMPORARY_WORLD_NAME_PREFIX,
                worldName,
                gameRoom.isRanked(),
                gameRoom.getNumber());

        SWMPlugin swmPlugin = SWMPlugin.getInstance();
        WorldData worldData = ConfigManager.getWorldConfig().getWorlds().get(worldName);
        SlimeLoader loader = swmPlugin.getLoader(worldData.getDataSource());
        CommandManager.getInstance().getWorldsInUse().add(worldName);

        return new AsyncTask<>((onFinish, onError) -> {
            try {
                swmPlugin.generateWorld(swmPlugin.loadWorld(loader, worldName, true, worldData.toPropertyMap())
                        .clone(targetWorldName, loader));

                world = Bukkit.getWorld(targetWorldName);

                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("월드 생성 실패 : {0}", ex, targetWorldName);
                onError.accept(ex);
            } finally {
                CommandManager.getInstance().getWorldsInUse().remove(targetWorldName);
            }
        });
    }

    /**
     * 생성된 복제본 월드를 삭제한다.
     */
    private void removeWorld() {
        String worldName = Validate.notNull(world).getName();
        Path path = Bukkit.getWorldContainer().toPath()
                .resolve(ConfigManager.getDatasourcesConfig().getFileConfig().getPath())
                .resolve(worldName + ".slime");

        new AsyncTask<>((onFinish, onError) -> {
            try {
                Bukkit.unloadWorld(world, false);
                Files.delete(path);

                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("월드 삭제 실패 : {0}", ex, worldName);
                onError.accept(ex);
            } finally {
                CommandManager.getInstance().getWorldsInUse().remove(worldName);
            }
        });
    }

    /**
     * 게임 참여자들의 MMR에 따라 팀을 나눈다.
     *
     * <p>팀 분배는 다음과 같이 이뤄진다.</p>
     *
     * <ol>
     * <li>1팀, 2팀을 무작위로 레드팀, 블루팀으로 지정한다.</li>
     * <li>플레이어 수를 {@code i}라고 하자. 먼저 최상위 플레이어({@code i / 4}의 내림값의 인원수) 를 1팀으로 이동시킨다.</li>
     * <li>그리고 중위권 플레이어({@code i / 2}의 내림값의 인원수) 를 2팀으로 이동시킨다.</li>
     * <li>나머지 하위권 플레이어를 1팀으로 전부 이동시킨다.</li>
     * </ol>
     */
    private void initTeamUsers() {
        ArrayList<User> sortedUsers = new ArrayList<>(gameRoom.getUsers());
        Collections.shuffle(sortedUsers);
        sortedUsers.sort(Comparator.comparingInt((User user) -> user.getUserData().getMatchMakingRate()).reversed());

        Team randomTeam = RandomUtils.nextBoolean() ? redTeam : blueTeam;

        int size = sortedUsers.size();
        for (int i = 0; i < size; i++) {
            User target = sortedUsers.get(i);
            Team team = (i < size / 4 || i >= (int) (size - size / 4.0)) ? randomTeam : randomTeam.getOppositeTeam();

            new GameUser(target, this, team);
        }
    }

    /**
     * 게임 준비 중 매 초마다 실행할 작업.
     *
     * @param remainingSeconds 남은 시간 (초)
     */
    private void onSecondReady(int remainingSeconds) {
        if (remainingSeconds > 0 && remainingSeconds <= 5)
            gameRoom.getUsers().forEach(user -> {
                TIMER_SOUND.play(user.getPlayer());
                user.sendTitle("§f" + remainingSeconds, "", Timespan.ZERO, Timespan.ofSeconds(0.25), Timespan.ofSeconds(0.5),
                        Timespan.ofSeconds(0.5));
            });
    }

    /**
     * 게임 진행 시 실행할 작업.
     */
    private void onPlay() {
        isPlaying = true;
        playTimestamp = Timestamp.now();
        endTimestamp = playTimestamp.plus(gamePlayMode.getPlayDuration());

        gamePlayModeScheduler.onPlay();

        gameRoom.getUsers().forEach(user -> {
            ON_PLAY_SOUND.play(user.getPlayer());

            user.sendTitle("§c§l전투 시작", "", Timespan.ZERO, Timespan.ofSeconds(2), Timespan.ofSeconds(1), Timespan.ofSeconds(2));
            user.sendMessageInfo("\n§n'/전체'§r 또는 §n'/tc'§r를 입력하여 팀/전체 채팅을 전환할 수 있습니다.\n");
        });

        onSecondTask = new IntervalTask(i -> {
            onSecondPlaying((int) Math.ceil(getRemainingTime().toSeconds()));

            return Timestamp.now().isBefore(endTimestamp);
        }, this::dispose, 20);
    }

    /**
     * 게임 진행 중 매 초마다 실행할 작업.
     *
     * @param remainingSeconds 남은 시간 (초)
     */
    private void onSecondPlaying(int remainingSeconds) {
        gamePlayModeScheduler.onSecond(remainingSeconds);

        int ultPackRemainingSeconds = (int) Math.ceil(GeneralConfig.getGameConfig().getUltPackActivationTime().minus(getElapsedTime()).toSeconds());

        if (ultPackRemainingSeconds >= 0)
            switch (ultPackRemainingSeconds) {
                case 20:
                case 10:
                case 5:
                    gameRoom.getUsers().forEach(user -> {
                        TIMER_SOUND.play(user.getPlayer());
                        user.sendTitle("", "§9궁극기 팩§f이 §e" + ultPackRemainingSeconds + "초 §f후 활성화됩니다.",
                                Timespan.ZERO, Timespan.ofSeconds(1), Timespan.ofSeconds(1), Timespan.ofSeconds(2));
                    });
                    break;
                case 0:
                    gameRoom.getUsers().forEach(user -> {
                        TIMER_SOUND.play(user.getPlayer());
                        user.sendTitle("", "§9궁극기 팩§f이 활성화되었습니다.", Timespan.ZERO, Timespan.ofSeconds(1),
                                Timespan.ofSeconds(1), Timespan.ofSeconds(2));
                    });
                    break;
                default:
                    break;
            }

        if (remainingSeconds > 0 && remainingSeconds <= 10)
            gameRoom.getUsers().forEach(user -> {
                TIMER_SOUND.play(user.getPlayer());
                user.sendTitle("", "§c" + remainingSeconds, Timespan.ZERO, Timespan.ofSeconds(0.25),
                        Timespan.ofSeconds(0.5), Timespan.ofSeconds(0.5));
            });
    }

    /**
     * 게임 플레이어 추가 시 실행할 작업.
     *
     * @param gameUser 대상 플레이어
     * @see GameUser#GameUser(User, Game, Team)
     */
    void onAddGameUser(@NonNull GameUser gameUser) {
        gameUsers.add(gameUser);
        gameUser.getTeam().teamUsers.add(gameUser);
    }

    /**
     * 게임 플레이어 제거 시 실행할 작업.
     *
     * @param gameUser 대상 플레이어
     * @see GameUser#dispose()
     */
    void onRemoveGameUser(@NonNull GameUser gameUser) {
        gameUsers.remove(gameUser);
        gameUser.getTeam().teamUsers.remove(gameUser);
    }

    /**
     * 게임에서 사용하는 팀 정보를 관리하는 클래스.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Team {
        /** 현재 게임 */
        private final Game game;
        /** 팀 종류 */
        @NonNull
        @Getter
        private final Team.Type type;
        /** 소속된 플레이어 목록 */
        private final HashSet<GameUser> teamUsers = new HashSet<>();
        /** 팀 점수 */
        @Getter
        private int score;

        /**
         * 상태 팀을 반환한다.
         *
         * @return 상대 팀
         */
        @NonNull
        public Team getOppositeTeam() {
            return type.oppositeTeamFunction.apply(game);
        }

        /**
         * 팀에 소속된 모든 플레이어 목록을 반환한다.
         *
         * @return 팀에 속한 모든 플레이어
         */
        @NonNull
        @UnmodifiableView
        public Set<@NonNull GameUser> getTeamUsers() {
            return Collections.unmodifiableSet(teamUsers);
        }

        /**
         * 현재 스폰 위치를 반환한다.
         *
         * @return 현재 스폰 위치
         */
        @NonNull
        public Location getSpawn() {
            return type.teamSpawnFunction.apply(game.gameMap)[game.gamePlayModeScheduler.getTeamSpawnIndex()]
                    .toLocation(Validate.notNull(game.world));
        }

        /**
         * 지정한 플레이어가 스폰 지역 안에 있는지 확인한다.
         *
         * @param gameUser 확인할 플레이어
         * @return 해당 플레이어가 팀 스폰 내부에 있으면 {@code true} 반환
         */
        public boolean isInSpawn(@NonNull GameUser gameUser) {
            return LocationUtil.isInSameBlockXZ(gameUser.getPlayer().getLocation(), GeneralConfig.getGameConfig().getSpawnRegionCheckYCoordinate(),
                    type.teamSpawnBlockType);
        }

        /**
         * 팀 점수를 1 증가시킨다.
         */
        public void addScore() {
            this.score += 1;
        }

        /**
         * 지정한 전투원을 선택한 팀원이 있는지 중복 여부를 확인한다.
         *
         * @param combatantType 확인할 전투원
         * @return 중복 여부
         */
        public boolean checkCombatantDuplication(@NonNull CombatantType combatantType) {
            return teamUsers.stream().anyMatch(targetGameUser -> {
                CombatUser targetCombatUser = CombatUser.fromUser(targetGameUser.getUser());
                return targetCombatUser != null && targetCombatUser.getCombatantType() == combatantType;
            });
        }

        /**
         * 팀 종류 (레드/블루).
         */
        @AllArgsConstructor
        public enum Type {
            RED(ChatColor.RED, "레드", GameMap::getRedTeamSpawns, Game::getBlueTeam, GeneralConfig.getGameConfig().getRedTeamSpawnBlock()),
            BLUE(ChatColor.BLUE, "블루", GameMap::getBlueTeamSpawns, Game::getRedTeam, GeneralConfig.getGameConfig().getBlueTeamSpawnBlock());

            /** 팀 색상 */
            @Getter
            private final ChatColor color;
            /** 이름 */
            @Getter
            private final String name;
            /** 팀 스폰 위치 목록 반환에 실행할 작업 */
            private final Function<GameMap, GlobalLocation[]> teamSpawnFunction;
            /** 상대 팀 반환에 실행할 작업 */
            private final Function<Game, Team> oppositeTeamFunction;
            /** 팀 스폰 식별 블록 타입 */
            private final Material teamSpawnBlockType;
        }
    }

    /**
     * 게임 종료 후 결과 처리 및 보상 지급을 관리하는 클래스.
     */
    private static final class ResultManager {
        /** 승리 효과음 */
        private static final SoundEffect WIN_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.UI_TOAST_CHALLENGE_COMPLETE).volume(1000).pitch(1.5).build());
        /** 패배 효과음 */
        private static final SoundEffect LOSE_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_BLAZE_DEATH).volume(1000).pitch(0.5).build());
        /** 무승부 효과음 */
        private static final SoundEffect DRAW_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_LEVELUP).volume(1000).pitch(1).build());

        /** 게임 */
        private final Game game;
        /** 승리한 팀. {@code null}이면 무승부 */
        @Nullable
        private final Team winnerTeam;
        /** 랭킹 항목별 플레이어 순위 목록 (랭킹 항목 : (플레이어 : 순위)) */
        private final EnumMap<RankType, HashMap<GameUser, Integer>> rankingMap = new EnumMap<>(RankType.class);

        /**
         * 게임 참여자들의 결과를 처리한다.
         *
         * @param game 대상 게임
         */
        private ResultManager(@NonNull Game game) {
            this.game = game;

            Team redTeam = game.getRedTeam();
            Team blueTeam = game.getBlueTeam();

            Team winner = redTeam.getScore() > blueTeam.getScore() ? redTeam : blueTeam;
            if (redTeam.getScore() == blueTeam.getScore())
                winner = null;

            this.winnerTeam = winner;

            for (Team team : new Team[]{redTeam, blueTeam})
                for (RankType rankType : RankType.values()) {
                    Iterator<GameUser> iterator = team.getTeamUsers().stream()
                            .sorted(Comparator.comparing(rankType.valueFunction::applyAsInt).reversed())
                            .iterator();
                    for (int i = 0; iterator.hasNext(); i++)
                        rankingMap.computeIfAbsent(rankType, k -> new HashMap<>()).put(iterator.next(), i);
                }

            game.getGameUsers().forEach(this::giveReward);
        }

        /**
         * 지정한 플레이어에게 보상을 지급한다.
         *
         * @param gameUser 대상 플레이어
         */
        private void giveReward(@NonNull GameUser gameUser) {
            if (winnerTeam != null) {
                UserData userData = gameUser.getUser().getUserData();

                if (winnerTeam == gameUser.getTeam())
                    userData.addWinCount();
                else
                    userData.addLoseCount();
            }

            int moneyEarned = updateMoney(gameUser);
            int xpEarned = updateXp(gameUser);
            int rankEarned = 0;

            if (game.gameRoom.isRanked())
                rankEarned = updateRankRate(gameUser);
            else
                updateMMR(gameUser);

            sendResultReport(gameUser, moneyEarned, xpEarned, rankEarned);
        }

        /**
         * 지정한 플레이어에게 전체 결과 메시지를 전송한다.
         *
         * @param gameUser    대상 플레이어
         * @param moneyEarned 획득한 돈
         * @param xpEarned    획득한 경험치
         * @param rankEarned  획득한 랭크 점수
         */
        private void sendResultReport(@NonNull GameUser gameUser, int moneyEarned, int xpEarned, int rankEarned) {
            ChatColor winColor;
            String winText;
            SoundEffect winSound;
            if (winnerTeam == null) {
                winColor = ChatColor.YELLOW;
                winText = "무승부";
                winSound = DRAW_SOUND;
            } else if (winnerTeam == gameUser.getTeam()) {
                winColor = ChatColor.GREEN;
                winText = "승리";
                winSound = WIN_SOUND;
            } else {
                winColor = ChatColor.RED;
                winText = "패배";
                winSound = LOSE_SOUND;
            }

            new DelayTask(() -> {
                gameUser.getUser().sendTitle(winColor + "§l" + winText, "", Timespan.ofSeconds(0.4), Timespan.ofSeconds(2),
                        Timespan.ofSeconds(1.5), Timespan.ofSeconds(2));
                winSound.play(gameUser.getPlayer());
            }, 40);

            StringBuilder result = new StringBuilder(MessageFormat.format(String.join("\n",
                    StringFormUtil.BAR,
                    "§d§l플레이 정보 {0}§l[{1}]",
                    "",
                    getPlayInfoReport(gameUser),
                    "",
                    "§d§l보상 획득",
                    "",
                    "§e▶ CP 획득 §7:: §6+{2}",
                    "§e▶ 경험치 획득 §7:: §6+{3}",
                    StringFormUtil.BAR), winColor, winText, moneyEarned, xpEarned));
            if (game.getGamePlayMode().isRanked())
                result.append(MessageFormat.format(String.join("\n",
                        "§d§l랭크",
                        "",
                        "§e▶ 랭크 점수 §7:: {0}",
                        StringFormUtil.BAR), winColor + (rankEarned >= 0 ? "+" : "") + rankEarned));

            gameUser.getUser().clearChat();
            gameUser.getUser().sendMessageInfo(result.toString());
        }

        /**
         * 지정한 플레이어의 플레이 정보 결과 메시지를 반환한다.
         *
         * @param gameUser 대상 플레이어
         * @return 결과 메시지
         */
        @NonNull
        private String getPlayInfoReport(@NonNull GameUser gameUser) {
            ChatColor[] rankColors = {ChatColor.YELLOW, ChatColor.WHITE, ChatColor.GOLD, ChatColor.DARK_GRAY};
            StringJoiner playInfoText = new StringJoiner("\n");

            rankingMap.keySet().forEach(type -> {
                int rankIndex = rankingMap.get(type).get(gameUser);

                String text = MessageFormat.format("{0}§l■ {0}{1} : {2} §l[{3}위]",
                        rankColors[Math.min(rankIndex, 3)],
                        type.name,
                        type.valueFunction.applyAsInt(gameUser),
                        rankIndex + 1);

                playInfoText.add(text);
            });
            playInfoText.add("§8§l■ §8사망 : " + gameUser.getDeath());

            return playInfoText.toString();
        }

        /**
         * 매치 종료 후 결과에 따라 플레이어의 경험치를 증가시킨다.
         *
         * @param gameUser 대상 플레이어
         * @return 획득한 경험치
         */
        private int updateXp(@NonNull GameUser gameUser) {
            UserData userData = gameUser.getUser().getUserData();

            int xp = userData.getXp();
            double score = gameUser.getScore();

            int finalXp = getFinalXp(xp, score, winnerTeam == gameUser.getTeam());
            userData.setXp(finalXp);

            return finalXp - xp;
        }

        /**
         * 매치 종료 후 결과에 따라 플레이어의 돈을 증가시킨다.
         *
         * @param gameUser 대상 플레이어
         * @return 획득한 돈
         */
        private int updateMoney(@NonNull GameUser gameUser) {
            UserData userData = gameUser.getUser().getUserData();

            int money = userData.getMoney();
            double score = gameUser.getScore();

            userData.setMoney(getFinalMoney(money, score, winnerTeam == gameUser.getTeam()));

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
            Timespan playTime = gameUser.getStartTime().until(Timestamp.now());
            int gameAverageMMR = (int) getAverage(UserData::getMatchMakingRate);

            userData.setMatchMakingRate(getFinalMMR(mmr, normalPlayCount, kda, score, playTime, gameAverageMMR));
            userData.addNormalPlayCount();

            ConsoleLogger.info("{0}의 유저 MMR 변동됨: {1} -> {2}, 일반 매치 플레이 횟수: {3}",
                    gameUser.getPlayer().getName(),
                    mmr, userData.getMatchMakingRate(),
                    normalPlayCount + 1);
        }

        /**
         * 랭크 매치 종료 후 결과에 따라 플레이어의 랭크 점수와 MMR을 조정한다.
         *
         * @param gameUser 대상 플레이어
         * @return 랭크 점수 획득량
         */
        private int updateRankRate(@NonNull GameUser gameUser) {
            UserData userData = gameUser.getUser().getUserData();

            int mmr = userData.getMatchMakingRate();
            int rr = userData.getRankRate();
            int rankPlayCount = userData.getRankPlayCount();
            double kda = gameUser.getKDARatio();
            double score = gameUser.getScore();
            Timespan playTime = gameUser.getStartTime().until(Timestamp.now());
            int gameAverageMMR = (int) getAverage(UserData::getMatchMakingRate);
            int gameAverageRR = (int) getAverage(UserData::getRankRate);

            userData.setMatchMakingRate(getFinalMMR(mmr, rankPlayCount, kda, score, playTime, gameAverageMMR));
            userData.addRankPlayCount();

            if (!userData.isRanked()) {
                if (rankPlayCount + 1 >= GeneralConfig.getGameConfig().getRankPlacementPlayCount()) {
                    userData.setRankRate(getFinalRankRate(mmr));
                    userData.setRanked(true);
                }
            } else {
                userData.setRankRate(getFinalRankRateRanked(mmr, rr, kda, score, playTime, gameAverageRR,
                        winnerTeam == null ? null : winnerTeam == gameUser.getTeam()));
                ConsoleLogger.info("{0}의 RR 변동됨: {1} -> {2}, 랭크 매치 플레이 횟수: {3}",
                        gameUser.getPlayer().getName(),
                        rr,
                        userData.getRankRate(),
                        rankPlayCount + 1);
            }

            return userData.getRankRate() - rr;
        }

        /**
         * 게임 참여자들의 특정 수치의 평균을 반환한다.
         *
         * @param valueFunction 수치 값 반환에 실행할 작업
         * @return 평균 값
         */
        private double getAverage(@NonNull ToIntFunction<@NonNull UserData> valueFunction) {
            return game.getGameUsers().stream()
                    .mapToInt(gameUser -> valueFunction.applyAsInt(gameUser.getUser().getUserData()))
                    .average()
                    .orElse(0);
        }

        /**
         * 게임 결과에 따른 최종 경험치를 반환한다.
         *
         * @param xp    현재 경험치
         * @param score 점수
         * @param isWin 승리 여부
         * @return 최종 경험치
         */
        private int getFinalXp(int xp, double score, boolean isWin) {
            int finalScore = (int) (xp + 50 + score * 0.2);
            if (isWin)
                finalScore += 200;

            return finalScore;
        }

        /**
         * 게임 결과에 따른 최종 금액을 반환한다.
         *
         * @param money 현재 보유 중인 돈
         * @param score 점수
         * @param isWin 승리 여부
         * @return 최종 금액
         */
        private int getFinalMoney(int money, double score, boolean isWin) {
            int finalScore = (int) (money + 50 + score * 0.2);
            if (isWin)
                finalScore += 200;

            return finalScore;
        }

        /**
         * 게임 결과에 따른 최종 MMR을 반환한다.
         *
         * @param mmr        현재 MMR
         * @param playCount  플레이 횟수
         * @param kda        킬/데스
         * @param score      점수
         * @param playTime   플레이 시간
         * @param averageMMR 게임 참여자들의 MMR 평균
         * @return 최종 MMR
         */
        private int getFinalMMR(int mmr, int playCount, double kda, double score, @NonNull Timespan playTime, int averageMMR) {
            int value = (int) Math.min(((getKDARatioCorrection(kda) + getScoreCorrection(score, playTime)) * 10 + averageMMR), 1000);

            double finalMMR;
            if (playCount < GeneralConfig.getGameConfig().getMmrPlayCountThreshold())
                finalMMR = (mmr * playCount / (playCount + 1.0)) + value * (1 / (playCount + 1.0));
            else {
                playCount = GeneralConfig.getGameConfig().getMmrPlayCountThreshold();
                finalMMR = (mmr * (playCount - 1.0) / (playCount)) + value * (1.0 / (playCount));
            }

            return (int) finalMMR;
        }

        /**
         * 게임 결과에 따른 최종 랭크 점수를 반환한다. (배치 미완료)
         *
         * @param mmr 현재 MMR
         * @return 최종 랭크 점수
         */
        private int getFinalRankRate(int mmr) {
            return (int) Math.min(mmr * 0.9, GeneralConfig.getGameConfig().getMaxPlacementRankRate());
        }

        /**
         * 게임 결과에 따른 최종 랭크 점수를 반환한다. (배치 완료)
         *
         * @param mmr       현재 MMR
         * @param rr        현재 랭크 점수
         * @param kda       킬/데스
         * @param score     점수
         * @param playTime  플레이 시간
         * @param averageRR 게임 참여자들의 랭크 점수 평균
         * @param isWin     승리 여부. {@code null}로 지정 시 무승부를 나타냄
         * @return 최종 랭크 점수
         */
        private int getFinalRankRateRanked(int mmr, int rr, double kda, double score, @NonNull Timespan playTime, int averageRR, @Nullable Boolean isWin) {
            return (int) (rr + Math.round(getKDARatioCorrection(kda) + getScoreCorrection(score, playTime) + getWinCorrection(isWin) +
                    getPointCorrection(mmr, rr, averageRR)));
        }

        /**
         * 킬/데스 보정치를 반환한다.
         *
         * @param kda 킬/데스
         * @return 킬/데스 보정치
         */
        private double getKDARatioCorrection(double kda) {
            return (kda / GeneralConfig.getGameConfig().getExpectedAverageKDARatio()) * 20;
        }

        /**
         * 게임 점수 보정치를 반환한다.
         *
         * @param score    점수
         * @param playTime 플레이 시간
         * @return 게임 점수 보정치
         */
        private double getScoreCorrection(double score, @NonNull Timespan playTime) {
            return ((score / GeneralConfig.getGameConfig().getExpectedAverageScorePerMinute()) / playTime.toSeconds() / 60) * 20;
        }

        /**
         * 승패 보정치를 반환한다.
         *
         * @param isWin 승리 여부. {@code null}로 지정 시 무승부를 나타냄
         * @return 승패 보정치
         */
        private double getWinCorrection(@Nullable Boolean isWin) {
            if (isWin == null)
                return 0;
            return isWin ? 10 : -8;
        }

        /**
         * MMR, 랭크 점수 보정치를 반환한다.
         *
         * @param mmr       MMR
         * @param rr        랭크 점수
         * @param averageRR 게임 참여자들의 랭크 점수 평균
         * @return MMR, 랭크 점수 보정치
         */
        private double getPointCorrection(int mmr, int rr, int averageRR) {
            double averageDiffValue = (GeneralConfig.getGameConfig().getExpectedAverageRankRate() + averageRR) / 2.0 - rr;
            int weightValue = mmr - rr;

            return averageDiffValue * 0.04 + weightValue * 0.1;
        }

        /**
         * 게임 랭킹 항목의 종류.
         */
        @AllArgsConstructor
        private enum RankType {
            SCORE("점수", gameUser -> (int) gameUser.getScore()),
            DAMAGE("입힌 피해", gameUser -> (int) gameUser.getDamage()),
            KILL("적 처치", GameUser::getKill),
            DEFEND("막은 피해", gameUser -> (int) gameUser.getDefend()),
            HEAL("치유", gameUser -> (int) gameUser.getHeal());

            /** 항목 이름 */
            private final String name;
            /** 항목 값 반환에 실행할 작업 */
            private final ToIntFunction<GameUser> valueFunction;
        }
    }
}
