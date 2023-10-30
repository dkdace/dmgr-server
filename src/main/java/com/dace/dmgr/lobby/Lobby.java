package com.dace.dmgr.lobby;

import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.StringFormUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 로비에서 사용하는 기능을 제공하는 클래스.
 */
public final class Lobby {
    /** 스폰 위치 */
    public static final Location lobbyLocation = new Location(Bukkit.getWorld("DMGR"), 72.5, 64, 39.5, 90, 0);

    /**
     * 로비 이동 이벤트.
     *
     * <p>명령어 또는 메뉴로 스폰 이동 시 호출해야 한다.</p>
     *
     * @param player 대상 플레이어
     */
    public static void spawn(Player player) {
        User user = EntityInfoRegistry.getUser(player);

        player.teleport(lobbyLocation);
        user.reset();
    }

    /**
     * 로비 스케쥴러를 실행한다.
     *
     * <p>플레이어가 로비에 있을 때 실행해야 한다.</p>
     *
     * @param user 대상 플레이어
     */
    public static void lobbyTick(User user) {
        Player player = user.getPlayer();

        TaskManager.addTask(user, new TaskTimer(20) {
            @Override
            public boolean onTimerTick(int i) {
                if (user.getUserConfig().isNightVision())
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 99999, 0, false, false));
                else
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);

                int reqXp = user.getNextLevelXp();
                int reqRank = user.getNextTierScore();
                int curRank = user.getCurrentTierScore();
                user.getLobbySidebar().clear();
                user.getLobbySidebar().setName("§b§n" + player.getName());
                user.getLobbySidebar().setAll(
                        "§f",
                        "§e보유 중인 돈",
                        "§6" + String.format("%,d", user.getMoney()),
                        "§f§f",
                        "§f레벨 : " + user.getLevelPrefix(),
                        StringFormUtil.getProgressBar(user.getXp(), reqXp, ChatColor.DARK_GREEN) + " §2[" + user.getXp() + "/" + reqXp + "]",
                        "§f§f§f",
                        "§f랭크 : " + user.getTierPrefix(),
                        StringFormUtil.getProgressBar(user.getRank() - curRank, reqRank - curRank, ChatColor.DARK_AQUA) + " §3[" + user.getRank() + "/" + reqRank + "]"
                );

                return true;
            }
        });
    }
}
