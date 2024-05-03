package com.dace.dmgr.game.scheduler;

import com.comphenix.packetwrapper.WrapperPlayServerBoss;
import com.dace.dmgr.game.Game;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.boss.BarColor;

import java.text.MessageFormat;

/**
 * 팀 데스매치 스케쥴러 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamDeathmatchScheduler implements GamePlayModeScheduler {
    @Getter
    private static final TeamDeathmatchScheduler instance = new TeamDeathmatchScheduler();

    @Override
    public void onSecond(@NonNull Game game) {
        broadcastBossBar(game);
    }

    @Override
    public int getRedTeamSpawnIndex() {
        return 0;
    }

    @Override
    public int getBlueTeamSpawnIndex() {
        return 0;
    }

    /**
     * 모든 플레이어에게 게임 진행 타이머 보스바를 전송한다.
     */
    private void broadcastBossBar(Game game) {
        String displayTime = (game.getRemainingTime() < 60 ? "§c§l" : "§l") +
                DurationFormatUtils.formatDuration(game.getRemainingTime() * 1000L, "mm:ss", true);

        game.getGameUsers().forEach(gameUser ->
                gameUser.getUser().addBossBar("RemainingTime", MessageFormat.format("§b남은 시간 : {0}", displayTime),
                        BarColor.BLUE, WrapperPlayServerBoss.BarStyle.PROGRESS, (double) game.getRemainingTime() / game.getGamePlayMode().getPlayDuration()));
    }
}
