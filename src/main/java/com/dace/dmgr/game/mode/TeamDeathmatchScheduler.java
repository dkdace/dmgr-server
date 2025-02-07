package com.dace.dmgr.game.mode;

import com.dace.dmgr.effect.BossBarDisplay;
import com.dace.dmgr.game.Game;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

/**
 * 팀 데스매치 스케쥴러 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class TeamDeathmatchScheduler implements GamePlayModeScheduler {
    /** 현재 게임 */
    private final Game game;
    /** 타이머 보스바 */
    private final BossBarDisplay timerBossBar = new BossBarDisplay("", BarColor.BLUE, BarStyle.SOLID, 1);

    @Override
    public void onPlay() {
        game.addBossBar(timerBossBar);
    }

    @Override
    public void onSecond(int remainingSeconds) {
        String displayTime = (remainingSeconds < 60 ? "§c§l" : "§l") +
                DurationFormatUtils.formatDuration(remainingSeconds * 1000L, "mm:ss", true);
        timerBossBar.setTitle("§b남은 시간 : " + displayTime);
        timerBossBar.setProgress(remainingSeconds / game.getGamePlayMode().getPlayDuration().toSeconds());
    }

    @Override
    public int getTeamSpawnIndex() {
        return 0;
    }
}
