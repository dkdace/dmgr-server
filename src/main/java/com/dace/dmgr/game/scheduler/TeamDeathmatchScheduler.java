package com.dace.dmgr.game.scheduler;

import com.dace.dmgr.game.Game;
import lombok.Getter;
import org.bukkit.Bukkit;

/**
 * 팀 데스매치 스케쥴러 클래스.
 */
public final class TeamDeathmatchScheduler implements GamePlayModeScheduler {
    @Getter
    private static final TeamDeathmatchScheduler instance = new TeamDeathmatchScheduler();

    @Override
    public void onSecond(Game game) {
        Bukkit.broadcastMessage("남은 시간 : " + game.getRemainingTime());
    }

    @Override
    public int getRedTeamSpawnIndex() {
        return 0;
    }

    @Override
    public int getBlueTeamSpawnIndex() {
        return 0;
    }
}
