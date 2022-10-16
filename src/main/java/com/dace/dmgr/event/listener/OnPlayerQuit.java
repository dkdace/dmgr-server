package com.dace.dmgr.event.listener;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.system.task.TaskWait;
import com.dace.dmgr.lobby.User;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.dace.dmgr.system.HashMapList.userMap;

public class OnPlayerQuit implements Listener {
    private static final String PREFIX = "§f§l[§6§l-§f§l] §b";

    @EventHandler
    public static void event(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = userMap.get(player);
        user.reset();

        event.setQuitMessage(PREFIX + player.getName());
        userMap.remove(player);

        new TaskWait(1) {
            @Override
            public void run() {
                DMGR.getPlugin().getServer().broadcastMessage(PREFIX + "현재 인원수는 §3§l" + Bukkit.getOnlinePlayers().size() + "명§b입니다.");
                playQuitSound();
            }
        };
    }

    private static void playQuitSound() {
        new TaskTimer(1, 3) {
            @Override
            public boolean run(int i) {
                switch (i) {
                    case 0:
                        SoundUtil.playAll(Sound.BLOCK_NOTE_PLING, 1000F, 0.8F);
                        return true;
                    case 3:
                        SoundUtil.playAll(Sound.BLOCK_NOTE_PLING, 1000F, 0.525F);
                        return true;
                }

                return true;
            }
        };
    }
}
