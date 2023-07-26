package com.dace.dmgr.event.listener;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.lobby.User;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.system.task.TaskWait;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerQuit implements Listener {
    /** 퇴장 메시지의 접두사 */
    private static final String PREFIX = "§f§l[§6§l-§f§l] §b";

    @EventHandler
    public static void event(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = EntityInfoRegistry.getUser(player);
        user.reset();

        event.setQuitMessage(PREFIX + player.getName());
        EntityInfoRegistry.removeUser(player);

        new TaskWait(1) {
            @Override
            public void run() {
                DMGR.getPlugin().getServer().broadcastMessage(PREFIX + "현재 인원수는 §3§l" + Bukkit.getOnlinePlayers().size() + "명§b입니다.");
                playQuitSound();
            }
        };
    }

    /**
     * 퇴장 효과음을 재생한다.
     */
    private static void playQuitSound() {
        new TaskTimer(1, 4) {
            @Override
            public boolean run(int i) {
                switch (i) {
                    case 0:
                        SoundUtil.playAll(Sound.BLOCK_NOTE_PLING, 1000F, 0.8F);
                        break;
                    case 3:
                        SoundUtil.playAll(Sound.BLOCK_NOTE_PLING, 1000F, 0.525F);
                        break;
                }

                return true;
            }
        };
    }
}
