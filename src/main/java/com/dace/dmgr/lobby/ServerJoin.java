package com.dace.dmgr.lobby;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.data.model.User;
import com.dace.dmgr.util.SoundPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ServerJoin {
    private static final String PREFIX = "§f§l[§a§l+§f§l] §b";
    private static final String TITLE = "§3Welcome to §b§lDMGR";

    public static void event(PlayerJoinEvent event, User user) {
        event.setJoinMessage(PREFIX + user.player.getName());

        new BukkitRunnable() {
            @Override
            public void run() {
                DMGR.getPlugin().getServer().broadcastMessage(PREFIX + "현재 인원수는 §3§l" + Bukkit.getOnlinePlayers().size() + "명§b입니다.");
                ResourcePack.sendResourcePack(user);

                user.player.sendTitle(TITLE, "", 0, 100, 40);
                playJoinSound();
            }
        }.runTaskLater(DMGR.getPlugin(), 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    user.player.sendMessage("§f");
                }
            }
        }.runTaskLater(DMGR.getPlugin(), 10);
    }

    private static void playJoinSound() {
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                switch (i++) {
                    case 0:
                        SoundPlayer.play(Sound.BLOCK_NOTE_PLING, 1000F, 0.7F);
                    case 3:
                        SoundPlayer.play(Sound.BLOCK_NOTE_PLING, 1000F, 1.05F);
                    case 4:
                        cancel();
                }
            }
        }.runTaskTimer(DMGR.getPlugin(), 0, 1);
    }
}
