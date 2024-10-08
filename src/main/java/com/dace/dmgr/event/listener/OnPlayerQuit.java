package com.dace.dmgr.event.listener;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.MessageFormat;

public final class OnPlayerQuit implements Listener {
    @EventHandler
    public static void event(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = User.fromPlayer(player);
        user.dispose();

        event.setQuitMessage(StringFormUtil.REMOVE_PREFIX + player.getName());

        new DelayTask(() -> {
            DMGR.getPlugin().getServer().broadcastMessage(MessageFormat.format("{0}현재 인원수는 §3§l{1}명§b입니다.",
                    StringFormUtil.REMOVE_PREFIX, Bukkit.getOnlinePlayers().size()));
            playQuitSound();
        }, 1);
    }

    /**
     * 퇴장 효과음을 재생한다.
     */
    private static void playQuitSound() {
        new IntervalTask(i -> {
            switch (i.intValue()) {
                case 0:
                    Bukkit.getOnlinePlayers().forEach(player -> SoundUtil.play(Sound.BLOCK_NOTE_PLING, player, 1000, Math.pow(2, -4 / 12.0)));
                    break;
                case 3:
                    Bukkit.getOnlinePlayers().forEach(player -> SoundUtil.play(Sound.BLOCK_NOTE_PLING, player, 1000, Math.pow(2, -11 / 12.0)));
                    break;
                default:
                    break;
            }

            return true;
        }, 1, 4);
    }
}
