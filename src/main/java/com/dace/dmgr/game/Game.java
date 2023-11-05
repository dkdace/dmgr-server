package com.dace.dmgr.game;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.lobby.Lobby;
import com.dace.dmgr.lobby.User;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.GameInfoRegistry;
import com.dace.dmgr.system.SystemPrefix;
import com.dace.dmgr.system.task.HasTask;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.WorldUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 일반 게임의 정보를 담고 관리하는 클래스.
 */
@Getter
public final class Game implements HasTask {
    /** 랭크가 결정되는 배치 판 수 */
    private static final int RANK_PLACEMENT_PLAY_COUNT = 5;

    /** 방 번호 */
    private final int number;
    /** 플레이어 목록 */
    private final ArrayList<GameUser> gameUsers = new ArrayList<>();
    /** 팀별 플레이어 목록 (팀 : 플레이어 목록) */
    private final EnumMap<Team, ArrayList<GameUser>> teamUserMap = new EnumMap<>(Team.class);
    /** 팀 점수 (팀 : 점수) */
    private final EnumMap<Team, Integer> teamScore = new EnumMap<>(Team.class);
    /** 게임 모드 */
    private final GameMode gameMode;
    /** 전장 월드 이름 */
    private final String worldName;
    /** 맵 */
    private final GameMap map;
    /** 게임 시작 시점 ({@link Phase#READY}가 된 시점) */
    private long startTime = 0;
    /** 다음 진행 단계까지 남은 시간 */
    @Setter
    private int remainingTime = 30;
    /** 게임 진행 단계 */
    @Setter
    private Phase phase = Phase.WAITING;

    /**
     * 게임 인스턴스를 생성한다.
     *
     * @param number   방 번호
     * @param gameMode 게임 모드
     */
    public Game(int number, GameMode gameMode) {
        this.number = number;
        this.gameMode = gameMode;
        this.map = GameInfoRegistry.getRandomMap(gameMode);
        this.worldName = WorldUtil.getRandomWorldName(map.getWorldName());
        for (Team team : Team.values()) {
            this.teamUserMap.put(team, new ArrayList<>());
            this.teamScore.put(team, 0);
        }
    }

    /**
     * 게임 정보를 초기화하고 스케쥴러를 실행한다.
     */
    public void init() {
        GameInfoRegistry.addGame(number, this);

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
        gameUsers.forEach(gameUser -> Lobby.spawn(gameUser.getPlayer()));

        unloadWorld();
        GameInfoRegistry.removeGame(this.gameMode, number);
        TaskManager.clearTask(this);
    }

    @Override
    public String getTaskIdentifier() {
        return "Game@" + gameMode.toString() + hashCode();
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
        WorldUtil.unloadWorld(worldName);
    }

    /**
     * 매 초마다 실행할 작업.
     */
    private void onSecond() {
        remainingTime--;

        switch (phase) {
            case WAITING: {
                if (gameMode.getMinPlayer() > gameUsers.size()) {
                    if (remainingTime == 0) {
                        phase = Phase.READY;
                        remainingTime = gameMode.getReadyDuration();

                        onStart();

                        break;
                    }
                    if (remainingTime == 3)
                        loadWorld();
                    if (remainingTime <= 5 || remainingTime == 10)
                        gameUsers.forEach(gameUser -> sendMessage(gameUser,
                                MessageFormat.format("게임이 {0}초 뒤에 시작합니다.", remainingTime)));
                } else
                    remainingTime = 30;

                break;
            }
            case READY: {
                if (remainingTime == 0) {
                    phase = Phase.PLAYING;
                    remainingTime = gameMode.getPlayDuration();

                    gameUsers.forEach(gameUser -> {
                        SoundUtil.play(Sound.ENTITY_WITHER_SPAWN, 10F, 1F, gameUser.getPlayer());
                        gameUser.getPlayer().sendTitle(ChatColor.RED.toString() + "게임 시작", "", 0, 40, 20);
                    });

                    break;
                }
                if (remainingTime <= 5) {
                    gameUsers.forEach(gameUser -> {
                        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F, gameUser.getPlayer());
                        gameUser.getPlayer().sendTitle(ChatColor.WHITE.toString() + remainingTime, "", 0, 5, 10);
                    });
                }

                break;
            }
            case PLAYING: {
                gameMode.getGameModeScheduler().onSecond(this);

                if (remainingTime == 0) {
                    phase = Phase.END;
                    onFinish();

                    break;
                }
                if (remainingTime <= 10) {
                    gameUsers.forEach(gameUser -> {
                        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F, gameUser.getPlayer());
                        gameUser.getPlayer().sendTitle("", ChatColor.RED.toString() + remainingTime, 0, 5, 10);
                    });
                }

                break;
            }
            case END:
                remove();
        }
    }

    /**
     * 게임 시작 시 실행할 작업.
     */
    private void onStart() {
        startTime = System.currentTimeMillis();
        gameUsers.forEach(gameUser -> {
            gameUser.getPlayer().sendTitle(gameMode.getName(), "", 10, gameMode.getReadyDuration() * 20, 0);
            CombatUser combatUser = new CombatUser(gameUser.getPlayer());
            combatUser.init();
        });
        divideTeam();

        gameUsers.forEach(gameUser -> gameUser.getPlayer().teleport(gameUser.getRespawnLocation()));
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
                        EntityInfoRegistry.getUser(((GameUser) gameUser).getPlayer()).getMatchMakingRate()).reversed())
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
        if (new Random().nextBoolean()) {
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
        gameUsers.forEach(gameUser -> {
            for (int i = 0; i < 100; i++)
                sendMessage(gameUser, "");
        });

        Team winnerTeam = teamScore.get(Team.RED) > teamScore.get(Team.BLUE) ? Team.RED : Team.BLUE;
        if (teamScore.get(Team.RED).equals(teamScore.get(Team.BLUE)))
            winnerTeam = Team.NONE;

        if (gameMode.isRanked()) {
            for (GameUser gameUser : gameUsers) {
                Boolean isWinner = winnerTeam == Team.NONE ? null : gameUser.getTeam() == winnerTeam;
                updateRankRate(gameUser, isWinner);
            }
        } else {
            for (GameUser gameUser : gameUsers)
                updateMMR(gameUser);
        }

        for (GameUser gameUser : gameUsers) {
            sendMessage(gameUser, gameMode.getName() + " 게임 결과");

            if (winnerTeam == Team.NONE)
                sendMessage(gameUser, "* " + winnerTeam.getColor() + "무승부");
            else
                sendMessage(gameUser, "* " + winnerTeam.getColor() + winnerTeam.name() + "팀 승리");
            sendMessage(gameUser, "");

            sendMessage(gameUser, "내 게임 성적");
            sendMessage(gameUser, "* 점수: " + gameUser.getScore());
            sendMessage(gameUser, "* K/D/A: " + gameUser.getKill() + "/" + gameUser.getDeath() + "/" + gameUser.getAssist() +
                    " (" + (gameUser.getKDARatio()) + ")");
            sendMessage(gameUser, "* 입힌 데미지: " + gameUser.getDamageDealt());
        }
    }

    /**
     * 일반 매치 종료 후 결과에 따라 플레이어의 MMR을 조정한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void updateMMR(GameUser gameUser) {
        User user = EntityInfoRegistry.getUser(gameUser.getPlayer());

        int mmr = user.getMatchMakingRate();
        int normalPlayCount = user.getNormalPlayCount();
        float kda = gameUser.getKDARatio();
        int score = gameUser.getScore();
        int playTime = (int) ((System.currentTimeMillis() - gameUser.getGame().getStartTime()) / 1000);
        int gameAverageMMR = (int) gameUser.getGame().getAverageMMR();

        user.setMatchMakingRate(RankUtil.getFinalMMR(mmr, normalPlayCount, kda, score, playTime, gameAverageMMR));
        user.setNormalPlayCount(normalPlayCount + 1);

        DMGR.getPlugin().getLogger().info(MessageFormat.format("유저 MMR 변동됨: ({0}) {1} -> {2} MMR PLAY: {3}",
                gameUser.getPlayer().getName(), mmr, user.getMatchMakingRate(), normalPlayCount));
    }

    /**
     * 랭크 매치 종료 후 결과에 따라 플레이어의 랭크 점수와 MMR을 조정한다.
     *
     * @param gameUser 대상 플레이어
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     */
    private void updateRankRate(GameUser gameUser, Boolean isWinner) {
        User user = EntityInfoRegistry.getUser(gameUser.getPlayer());

        int mmr = user.getMatchMakingRate();
        int rr = user.getRankRate();
        int rankPlayCount = user.getRankPlayCount();
        float kda = gameUser.getKDARatio();
        int score = gameUser.getScore();
        int playTime = (int) ((System.currentTimeMillis() - gameUser.getGame().getStartTime()) / 1000);
        int gameAverageMMR = (int) gameUser.getGame().getAverageMMR();
        int gameAverageRank = (int) gameUser.getGame().getAverageRankRate();

        user.setMatchMakingRate(RankUtil.getFinalMMR(mmr, rankPlayCount, kda, score, playTime, gameAverageMMR));

        if (!user.isRanked()) {
            if (rankPlayCount >= RANK_PLACEMENT_PLAY_COUNT) {
                user.setRankRate(RankUtil.getFinalRankRate(mmr));
                user.setRanked(true);
            }
        } else
            user.setRankRate(RankUtil.getFinalRankRateRanked(mmr, rr, kda, score, playTime, gameAverageRank, isWinner));
    }

    /**
     * 게임에 지정한 플레이어를 추가한다.
     *
     * <p>게임이 진행 중이면 인원이 부족한 팀이 있을 때만 난입이 가능하다.</p>
     *
     * @param gameUser 대상 플레이어
     */
    public void addPlayer(GameUser gameUser) {
        if (phase == Phase.WAITING) {
            gameUsers.add(gameUser);
            gameUsers.forEach(gameUser2 -> sendMessage(gameUser2, MESSAGES.JOIN_PREFIX + gameUser.getPlayer().getName()));
        } else {
            int redAmount = teamUserMap.get(Team.RED).size();
            int blueAmount = teamUserMap.get(Team.BLUE).size();

            if (redAmount != blueAmount) {
                if (redAmount < blueAmount)
                    gameUser.setTeam(Team.RED);
                else if (redAmount > blueAmount)
                    gameUser.setTeam(Team.BLUE);

                gameUsers.add(gameUser);
                teamUserMap.get(gameUser.getTeam()).add(gameUser);
            }
        }
    }

    /**
     * 게임에서 지정한 플레이어를 제거한다.
     *
     * @param gameUser 대상 플레이어
     */
    public void removePlayer(GameUser gameUser) {
        gameUsers.forEach(gameUser2 -> sendMessage(gameUser2, MESSAGES.QUIT_PREFIX + gameUser.getPlayer().getName()));
        gameUsers.remove(gameUser);
        teamUserMap.get(gameUser.getTeam()).remove(gameUser);
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
                .mapToInt(gameUser -> EntityInfoRegistry.getUser(gameUser.getPlayer()).getMatchMakingRate())
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
                .mapToInt(gameUser -> EntityInfoRegistry.getUser(gameUser.getPlayer()).getRankRate())
                .average()
                .orElse(0);
    }

    /**
     * 게임 진행 단계 목록.
     */
    public enum Phase {
        /** 인원 대기 */
        WAITING,
        /** 준비 */
        READY,
        /** 진행 */
        PLAYING,
        /** 종료 */
        END
    }

    /**
     * 게임에 사용되는 메시지 목록.
     */
    private interface MESSAGES {
        /** 입장 메시지의 접두사 */
        String JOIN_PREFIX = "§f§l[§a§l+§f§l] §b";
        /** 퇴장 메시지의 접두사 */
        String QUIT_PREFIX = "§f§l[§6§l-§f§l] §b";
    }
}
