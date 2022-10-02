package com.dace.dmgr.lobby;

import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static com.dace.dmgr.system.HashMapList.userMap;

public class Lobby {
    public static Location lobby = new Location(Bukkit.getWorld("DMGR"), 72.5, 64, 39.5, 90, 0);

    public static void spawn(Player player) {
        User user = userMap.get(player);

        player.teleport(lobby);
        user.reset();
    }

    public static void lobbyTick(Player player) {
        User user = userMap.get(player);

        new TaskTimer(20) {
            @Override
            public boolean run(int i) {
                if (userMap.get(player) == null)
                    return false;

                if (user.getUserConfig().isNightVision())
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 99999, 0, false, false));
                else
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);

                int reqXp = user.getNextLevelXp();
                int reqRank = user.getNextTierScore();
                int curRank = user.getCurrentTierScore();
                user.getLobbySidebar().setName("§b§n" + player.getName());
                user.getLobbySidebar().setAll(
                        "§f",
                        "§e보유 중인 돈",
                        "§6" + String.format("%,d", user.getMoney()),
                        "§f§f",
                        "§f레벨 : " + user.getLevelPrefix(),
                        StringUtil.getBar(user.getXp(), reqXp, ChatColor.DARK_GREEN) + " §2[" + user.getXp() + "/" + reqXp + "]",
                        "§f§f§f",
                        "§f랭크 : " + user.getTierPrefix(),
                        StringUtil.getBar(user.getRank() - curRank, reqRank - curRank, ChatColor.DARK_AQUA) + " §3[" + user.getRank() + "/" + reqRank + "]"
                );

                return true;
            }
        };
    }
}
