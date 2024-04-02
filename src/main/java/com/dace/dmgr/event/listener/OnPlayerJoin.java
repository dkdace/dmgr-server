package com.dace.dmgr.event.listener;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.MessageFormat;

public final class OnPlayerJoin implements Listener {
    @EventHandler
    public static void event(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = User.fromPlayer(player);
        UserData userData = UserData.fromPlayer(player);

        event.setJoinMessage(StringFormUtil.ADD_PREFIX + player.getName());

        user.sendTitle("§bWelcome!", "", 0, 50, 40);
        TaskUtil.addTask(user, new DelayTask(user::clearChat, 10));

        if (!userData.isInitialized())
            userData.init();

        new DelayTask(() -> {
            DMGR.getPlugin().getServer().broadcastMessage(MessageFormat.format("{0}현재 인원수는 §3§l{1}명§b입니다.",
                    StringFormUtil.ADD_PREFIX, Bukkit.getOnlinePlayers().size()));
            playJoinSound();
        }, 1);
    }

    /**
     * 입장 효과음을 재생한다.
     */
    private static void playJoinSound() {
        new IntervalTask(i -> {
            switch (i.intValue()) {
                case 0:
                    SoundUtil.broadcast(Sound.BLOCK_NOTE_PLING, 1000, 0.7);
                    break;
                case 3:
                    SoundUtil.broadcast(Sound.BLOCK_NOTE_PLING, 1000, 1.05);
                    break;
            }

            return true;
        }, 1, 4);
    }
}
