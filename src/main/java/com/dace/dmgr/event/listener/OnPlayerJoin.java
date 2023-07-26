package com.dace.dmgr.event.listener;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.lobby.User;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.system.task.TaskWait;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {
    /** 입장 메시지의 접두사 */
    private static final String PREFIX = "§f§l[§a§l+§f§l] §b";
    /** 입장 시 타이틀 메시지 */
    private static final String TITLE = "§3Welcome to §b§lDMGR";

    @EventHandler
    public static void event(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = new User(player);
        user.init();

        event.setJoinMessage(PREFIX + player.getName());

        if (!user.getUserConfig().isKoreanChat())
            player.performCommand("kakc chmod 0");

        new TaskWait(1) {
            @Override
            public void run() {
                DMGR.getPlugin().getServer().broadcastMessage(PREFIX + "현재 인원수는 §3§l" + Bukkit.getOnlinePlayers().size() + "명§b입니다.");

                player.sendTitle(TITLE, "", 0, 100, 40);
                playJoinSound();
            }
        };

        new TaskWait(10) {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    player.sendMessage("§f");
                }
                OnPlayerResourcePackStatus.sendResourcePack(player);
            }
        };
    }

    /**
     * 입장 효과음을 재생한다.
     */
    private static void playJoinSound() {
        new TaskTimer(1, 4) {
            @Override
            public boolean run(int i) {
                switch (i) {
                    case 0:
                        SoundUtil.playAll(Sound.BLOCK_NOTE_PLING, 1000F, 0.7F);
                        break;
                    case 3:
                        SoundUtil.playAll(Sound.BLOCK_NOTE_PLING, 1000F, 1.05F);
                        break;
                }

                return true;
            }
        };
    }
}
