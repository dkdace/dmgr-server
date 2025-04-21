package com.dace.dmgr.game;

import com.dace.dmgr.*;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.effect.BossBarDisplay;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.game.mode.GamePlayMode;
import com.dace.dmgr.game.mode.GamePlayModeScheduler;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.AsyncTask;
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

/**
 * 게임의 내부 진행 시스템을 관리하는 클래스.
 */
public final class Game implements Initializable<Void> {
    /** 타이머 효과음 */
    private static final SoundEffect TIMER_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(1000).pitch(1).build());
    /** 전투 시작 효과음 */
    private static final SoundEffect ON_PLAY_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_WITHER_SPAWN).volume(1000).pitch(1).build());

    /** 현재 게임 방 인스턴스 */
    private final GameRoom gameRoom;
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
    /** 종료 여부 */
    @Getter
    private boolean isFinished = false;
    /** 궁극기 팩 활성화 여부 */
    @Getter
    private boolean isUltPackActivated = false;

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
                        remove();
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
                    remove();
                });
    }

    /**
     * 게임 시스템을 종료하고 게임 방을 제거한다.
     *
     * @throws IllegalStateException 이미 종료되었으면 발생
     */
    void remove() {
        Validate.validState(!isFinished, "Game이 이미 종료됨");
        if (isInitialized())
            validate();

        if (!gameUsers.isEmpty()) {
            new ResultManager(this);
            new ArrayList<>(gameUsers).forEach(GameUser::remove);
        }

        isPlaying = false;
        isFinished = true;
        gameRoom.remove();

        if (onSecondTask != null)
            onSecondTask.stop();
        if (world != null)
            removeWorld();
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
        if (isFinished)
            return Timespan.ZERO;

        if (isPlaying())
            return Timestamp.now().until(endTimestamp);
        else
            return gamePlayMode.getPlayDuration();
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
        }, this::remove, 20);
    }

    /**
     * 게임 진행 중 매 초마다 실행할 작업.
     *
     * @param remainingSeconds 남은 시간 (초)
     */
    private void onSecondPlaying(int remainingSeconds) {
        gamePlayModeScheduler.onSecond(remainingSeconds);

        if (!isUltPackActivated) {
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
                        isUltPackActivated = true;

                        gameRoom.getUsers().forEach(user -> {
                            TIMER_SOUND.play(user.getPlayer());
                            user.sendTitle("", "§9궁극기 팩§f이 활성화되었습니다.", Timespan.ZERO, Timespan.ofSeconds(1),
                                    Timespan.ofSeconds(1), Timespan.ofSeconds(2));
                        });
                        break;
                    default:
                        break;
                }
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
     * @see GameUser#remove()
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
            return type.teamSpawnFunction.apply(game.gameMap)[game.gamePlayModeScheduler.getTeamSpawnIndex()].toLocation(Validate.notNull(game.world));
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
}
