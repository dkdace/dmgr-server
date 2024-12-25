package com.dace.dmgr.game.scheduler;

import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.BossBarDisplay;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.text.MessageFormat;
import java.util.WeakHashMap;

/**
 * 팀 데스매치 스케쥴러 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamDeathmatchScheduler implements GamePlayModeScheduler {
    @Getter
    private static final TeamDeathmatchScheduler instance = new TeamDeathmatchScheduler();
    /** 타이머 보스바 목록 (게임 : 보스바) */
    private static final WeakHashMap<Game, BossBarDisplay> TIMER_BOSS_BAR_MAP = new WeakHashMap<>();

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
    private void broadcastBossBar(@NonNull Game game) {
        BossBarDisplay bossBarDisplay = TIMER_BOSS_BAR_MAP.computeIfAbsent(game, k ->
                new BossBarDisplay("", BarColor.BLUE, BarStyle.SOLID, 1));

        String displayTime = (game.getRemainingTime() < 60 ? "§c§l" : "§l") +
                DurationFormatUtils.formatDuration(game.getRemainingTime() * 1000L, "mm:ss", true);
        bossBarDisplay.setTitle(MessageFormat.format("§b남은 시간 : {0}", displayTime));
        bossBarDisplay.setProgress((double) game.getRemainingTime() / game.getGamePlayMode().getPlayDuration());

        Bukkit.getOnlinePlayers().forEach(player -> {
            GameUser gameUser = GameUser.fromUser(User.fromPlayer(player));
            if (gameUser != null && gameUser.getGame() == game)
                bossBarDisplay.show(player);
            else
                bossBarDisplay.hide(player);
        });
    }
}
