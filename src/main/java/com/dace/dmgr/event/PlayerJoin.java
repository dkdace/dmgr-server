package com.dace.dmgr.event;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.user.Lobby;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.SoundPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoin {
    private static final String PREFIX = "§f§l[§a§l+§f§l] §b";
    private static final String TITLE = "§3Welcome to §b§lDMGR";

    public static void event(PlayerJoinEvent event, Player player) {
        User user = new User(player);

        Lobby.lobbyTick(player);
        event.setJoinMessage(PREFIX + player.getName());

        if (!user.getUserConfig().isKoreanChat())
            player.performCommand("kakc chmod 0");

        new BukkitRunnable() {
            @Override
            public void run() {
                DMGR.getPlugin().getServer().broadcastMessage(PREFIX + "현재 인원수는 §3§l" + Bukkit.getOnlinePlayers().size() + "명§b입니다.");
                PlayerResourcePack.sendResourcePack(user);

                player.sendTitle(TITLE, "", 0, 100, 40);
                playJoinSound();
            }
        }.runTaskLater(DMGR.getPlugin(), 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    player.sendMessage("§f");
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
