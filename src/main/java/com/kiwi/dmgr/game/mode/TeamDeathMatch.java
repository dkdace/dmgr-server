package com.kiwi.dmgr.game.mode;

import com.dace.dmgr.system.task.TaskTimer;
import com.kiwi.dmgr.game.Game;
import com.kiwi.dmgr.game.GameUser;
import com.kiwi.dmgr.game.Team;
import com.kiwi.dmgr.match.MatchType;
import org.bukkit.entity.Player;

import static com.kiwi.dmgr.game.GameMapList.addMatchMode;
import static com.kiwi.dmgr.game.GameMapList.gameUserMap;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

/**
 * 팀데스매치 클래스
 */
public class TeamDeathMatch extends GameMode implements IGameMode {

    private static final boolean VERSE_TEAM = true;
    private static final int MAX_PLAYER = 10;
    private static final int PLAY_TIME = 600;

    public TeamDeathMatch() {
        super(VERSE_TEAM, MAX_PLAYER, PLAY_TIME);
        addMatchMode(MatchType.UNRANKED, this);
        addMatchMode(MatchType.COMPETITIVE, this);
    }

    @Override
    public boolean isStartAble(int playerCount) {
        return playerCount > 0 && playerCount % 2 == 0;
    }

    @Override
    public void run(Game game) {
        game.sendAlertMessage("팀 데스매치");
        game.sendAlertMessage("전투를 준비하십시오.");

        /* 게임 모드 준비 스케쥴러 */
        new TaskTimer(20) {
            int startTimer = 30;

            @Override
            public boolean run(int i) {
                if (startTimer <= 5) {
                    if (startTimer == 0)
                        return false;
                    game.sendAlertMessage(String.valueOf(startTimer));
                }
                startTimer -= 1;
                return true;
            }
        };
        game.sendAlertMessage("전투 시작");
        game.sendAlertMessage("적을 처치하십시오.");
        /* 게임 모드 진행 스케쥴러 */
        new TaskTimer(2) {
            @Override
            public boolean run(int i) {
                long time = game.getRemainTime();
                if (time == 300) {
                    game.sendAlertMessage("게임 종료까지 5분 남았습니다.");
                }
                if (time == 60) {
                    game.sendAlertMessage("게임 종료까지 1분 남았습니다.");
                }
                if (time == 30) {
                    game.sendAlertMessage("게임 종료까지 30초 남았습니다.");
                }
                if (time == 10) {
                    game.sendAlertMessage("게임 종료까지 10초 남았습니다.");
                }
                if (time < 0) {
                    return false;
                }
                int redScore = 0;
                int blueScore = 0;
                for (Player player : game.getTeamPlayerMapList().get(Team.RED)) {
                    GameUser user = gameUserMap.get(player);
                    blueScore += user.getDeath();
                }
                for (Player player : game.getTeamPlayerMapList().get(Team.BLUE)) {
                    GameUser user = gameUserMap.get(player);
                    redScore += user.getDeath();
                }
                game.getTeamScore().put(Team.RED, redScore);
                game.getTeamScore().put(Team.BLUE, blueScore);
                game.setRemainTime((long) (time - 0.1));
                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                game.finish(false);
            }
        };
    }

    /*
    @Override
    public Scoreboard getScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective o = board.registerNewObjective("팀 데스매치", "dummy");
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        o.setDisplayName("§4팀 데스매치");
        Score s1 = o.getScore("§8--------");
        s1.setScore(3);
        Score s2 = o.getScore("   ");
        s2.setScore(2);
    }
     */
}
