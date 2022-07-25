package com.dace.dmgr.event;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.SoundPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static com.dace.dmgr.system.EntityList.userList;

public class ServerQuit {
    private static final String PREFIX = "§f§l[§6§l-§f§l] §b";

    public static void event(PlayerQuitEvent event, Player player) {
        User user = userList.get(player.getUniqueId());

        user.reset();

        event.setQuitMessage(PREFIX + player.getName());

        new BukkitRunnable() {
            @Override
            public void run() {
                DMGR.getPlugin().getServer().broadcastMessage(PREFIX + "현재 인원수는 §3§l" + Bukkit.getOnlinePlayers().size() + "명§b입니다.");

                playQuitSound();
            }
        }.runTaskLater(DMGR.getPlugin(), 1);
    }

    private static void playQuitSound() {
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                switch (i++) {
                    case 0:
                        SoundPlayer.play(Sound.BLOCK_NOTE_PLING, 1000F, 0.8F);
                    case 3:
                        SoundPlayer.play(Sound.BLOCK_NOTE_PLING, 1000F, 0.525F);
                    case 4:
                        cancel();
                }
            }
        }.runTaskTimer(DMGR.getPlugin(), 0, 1);
    }
}
