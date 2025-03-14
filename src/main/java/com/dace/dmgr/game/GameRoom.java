package com.dace.dmgr.game;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.effect.BossBarDisplay;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.text.MessageFormat;
import java.util.*;

/**
 * 게임 방의 정보와 상태를 관리하는 클래스.
 *
 * @see Game
 */
public final class GameRoom {
    /** 게임 방 목록 ((랭크 여부 : 방 번호) : 게임 방) */
    private static final HashMap<Pair<Boolean, Integer>, GameRoom> GAME_ROOM_MAP = new HashMap<>();

    /** 게임 시작 대기 보스바 목록 */
    private final BossBarDisplay[] gameWaitBossBars = new BossBarDisplay[]{
            new BossBarDisplay("§f대기열에서 나가려면 §n'/quit'§f 또는 §n'/q'§f를 입력하십시오."),
            new BossBarDisplay(""),
            new BossBarDisplay("", BarColor.GREEN, BarStyle.SOLID)
    };

    /** (랭크 여부 : 방 번호) */
    private final Pair<Boolean, Integer> pair;
    /** 소속된 플레이어 목록 */
    private final HashSet<User> users = new HashSet<>();
    /** 전체 보스바 목록 */
    private final ArrayList<BossBarDisplay> bossBars = new ArrayList<>();
    /** 게임을 시작하기 위한 최소 인원 수 */
    private final int minPlayerCount;
    /** 최대 수용 가능 인원 수 */
    private final int maxPlayerCount;
    /** 게임 시스템 */
    @NonNull
    @Getter
    private final Game game;

    /** 인원 대기 작업을 처리하는 태스크 */
    @Nullable
    private IntervalTask onSecondTask;
    /** 게임 진행 단계 */
    @NonNull
    @Getter
    private Phase phase = Phase.WAITING;

    /**
     * 게임 방 인스턴스를 생성한다.
     *
     * @param isRanked 랭크 여부
     * @param number   방 번호
     * @throws IllegalStateException 해당 {@code isRanked}, {@code number}의 GameRoom이 이미 존재하면 발생
     */
    public GameRoom(boolean isRanked, int number) {
        Pair<Boolean, Integer> checkPair = Pair.of(isRanked, number);
        Validate.validState(!GAME_ROOM_MAP.containsKey(checkPair), "GameRoom이 이미 존재함");

        this.pair = checkPair;
        this.minPlayerCount = isRanked ? GeneralConfig.getGameConfig().getRankMinPlayerCount() : GeneralConfig.getGameConfig().getNormalMinPlayerCount();
        this.maxPlayerCount = isRanked ? GeneralConfig.getGameConfig().getRankMaxPlayerCount() : GeneralConfig.getGameConfig().getNormalMaxPlayerCount();
        this.game = new Game(this);

        GAME_ROOM_MAP.put(pair, this);

        for (BossBarDisplay gameWaitBossBar : gameWaitBossBars)
            addBossBar(gameWaitBossBar);
    }

    /**
     * 지정한 랭크 여부 및 번호의 게임 방 인스턴스를 반환한다.
     *
     * @param isRanked 랭크 여부
     * @param number   방 번호
     * @return 게임 방 인스턴스. 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    public static GameRoom fromNumber(boolean isRanked, int number) {
        return GAME_ROOM_MAP.get(Pair.of(isRanked, number));
    }

    /**
     * @return 랭크 여부
     */
    public boolean isRanked() {
        return pair.getLeft();
    }

    /**
     * @return 방 번호
     */
    public int getNumber() {
        return pair.getRight();
    }

    /**
     * 게임 방에 소속된 모든 플레이어 목록을 반환한다.
     *
     * @return 게임 방에 속한 모든 플레이어
     */
    @NonNull
    @UnmodifiableView
    public Set<@NonNull User> getUsers() {
        return Collections.unmodifiableSet(users);
    }

    /**
     * 게임의 모든 참여자에게 표시되는 전체 보스바를 추가한다.
     *
     * @param bossBarDisplay 추가할 보스바
     */
    public void addBossBar(@NonNull BossBarDisplay bossBarDisplay) {
        users.forEach(gameUser -> bossBarDisplay.show(gameUser.getPlayer()));
        bossBars.add(bossBarDisplay);
    }

    /**
     * 게임의 모든 참여자에게 표시되는 전체 보스바를 제거한다.
     *
     * @param bossBarDisplay 제거할 보스바
     */
    public void removeBossBar(@NonNull BossBarDisplay bossBarDisplay) {
        users.forEach(gameUser -> bossBarDisplay.hide(gameUser.getPlayer()));
        bossBars.remove(bossBarDisplay);
    }

    /**
     * 게임을 시작할 조건이 충족되었는지 확인한다.
     *
     * @return 게임을 시작할 수 있으면 {@code true} 반환
     */
    public boolean canStart() {
        return users.size() >= minPlayerCount && (users.size() % 2 == 0);
    }

    /**
     * 플레이어가 게임에 참여할 수 있는지 확인한다.
     *
     * <p>게임이 진행 중이면 추가 인원을 수용할 수 있고 양 팀의 인원수가 다를 때 참여 가능하다.</p>
     *
     * @return 참여 가능 여부
     */
    public boolean canJoin() {
        if (users.size() >= maxPlayerCount || phase == Phase.FINISHED)
            return false;

        if (game.isInitialized())
            return !isRanked() && game.getRedTeam().getTeamUsers().size() != game.getBlueTeam().getTeamUsers().size();

        return true;
    }

    /**
     * 게임에 플레이어 입장 시 실행할 작업.
     *
     * @param user 입장한 플레이어
     * @see User#joinGame(GameRoom)
     */
    public void onJoin(@NonNull User user) {
        users.add(user);
        users.forEach(target -> target.sendMessageInfo(StringFormUtil.ADD_PREFIX + user.getPlayer().getName()));

        bossBars.forEach(bossBarDisplay -> bossBarDisplay.show(user.getPlayer()));

        onUserCountChange();

        if (game.isInitialized() && !game.isFinished()) {
            Game.Team team = game.getRedTeam().getTeamUsers().size() < game.getBlueTeam().getTeamUsers().size()
                    ? game.getRedTeam()
                    : game.getBlueTeam();

            new GameUser(user, game, team);
        }
    }

    /**
     * 게임에서 플레이어 퇴장 시 실행할 작업.
     *
     * @param user 퇴장한 플레이어
     * @see User#quitGame()
     */
    public void onQuit(@NonNull User user) {
        users.forEach(target -> target.sendMessageInfo(StringFormUtil.REMOVE_PREFIX + user.getPlayer().getName()));
        users.remove(user);

        bossBars.forEach(bossBarDisplay -> bossBarDisplay.hide(user.getPlayer()));

        onUserCountChange();

        if (game.isInitialized() && !game.isFinished())
            Validate.notNull(GameUser.fromUser(user)).remove();
        if (users.isEmpty())
            remove();
    }

    /**
     * 게임 인원수가 변경됐을 때 인원 대기 태스크를 설정한다.
     */
    private void onUserCountChange() {
        if (phase != Phase.WAITING)
            return;

        int waitingSeconds = (int) GeneralConfig.getGameConfig().getWaitingTime().toSeconds();

        if (canStart()) {
            if (onSecondTask == null)
                onSecondTask = new IntervalTask(i -> onSecondWaiting((int) (waitingSeconds - i)), () -> {
                    for (BossBarDisplay gameWaitBossBar : gameWaitBossBars)
                        removeBossBar(gameWaitBossBar);

                    phase = Phase.PLAYING;
                    game.init();

                    onSecondTask = null;
                }, 20, waitingSeconds + 1L);

            return;
        }

        onSecondWaiting(0);

        if (onSecondTask != null) {
            onSecondTask.stop();
            onSecondTask = null;
        }
    }

    /**
     * 인원 대기 중 매 초마다 실행할 작업.
     *
     * @param remainingSeconds 남은 시간 (초)
     */
    private void onSecondWaiting(int remainingSeconds) {
        gameWaitBossBars[1].setTitle(MessageFormat.format("§c게임을 시작하려면 최소 {0}명이 필요합니다.", minPlayerCount));
        gameWaitBossBars[2].setTitle(MessageFormat.format("§a§l{0} §f[{1}§f/{2} 명]",
                (isRanked() ? "§6§l랭크" : "§a§l일반"),
                (canStart() ? "§f" : "§c") + users.size(),
                maxPlayerCount));
        gameWaitBossBars[2].setProgress(canStart() ? remainingSeconds / GeneralConfig.getGameConfig().getWaitingTime().toSeconds() : 1);

        if (remainingSeconds > 0 && (remainingSeconds <= 5 || remainingSeconds == 10))
            users.forEach(user -> user.sendMessageInfo("게임이 {0}초 뒤에 시작합니다.", remainingSeconds));
    }

    /**
     * 게임 방을 제거한다.
     *
     * @throws IllegalStateException 이미 제거되었으면 발생
     */
    public void remove() {
        Validate.validState(GAME_ROOM_MAP.containsKey(pair), "GameRoom이 이미 제거됨");

        if (phase == Phase.FINISHED)
            return;

        phase = Phase.FINISHED;

        if (onSecondTask != null)
            onSecondTask.stop();
        if (game.isInitialized() && !game.isFinished())
            game.remove();

        if (!users.isEmpty())
            new ArrayList<>(users).forEach(User::quitGame);

        GAME_ROOM_MAP.remove(pair);
    }

    /**
     * 게임 진행 단계 목록.
     */
    @AllArgsConstructor
    public enum Phase {
        /** 인원 대기 */
        WAITING,
        /** 진행 */
        PLAYING,
        /** 종료 */
        FINISHED
    }
}
