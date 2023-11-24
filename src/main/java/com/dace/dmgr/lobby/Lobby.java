package com.dace.dmgr.lobby;

import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
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

        LocationUtil.teleportPlayer(player, lobbyLocation);
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
        UserData userData = user.getUserData();

        TaskManager.addTask(user, new TaskTimer(20) {
            @Override
            public boolean onTimerTick(int i) {
                if (userData.getUserConfig().isNightVision())
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 99999, 0, false, false));
                else
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);

                if (EntityInfoRegistry.getCombatUser(player) == null) {
                    int reqXp = userData.getNextLevelXp();
                    int rank = userData.isRanked() ? userData.getRankRate() : 0;
                    int reqRank = userData.getTier().getMaxScore();
                    int curRank = userData.getTier().getMinScore();

                    switch (userData.getTier()) {
                        case STONE:
                            curRank = userData.getTier().getMaxScore();
                            break;
                        case DIAMOND:
                        case NETHERITE:
                            reqRank = userData.getTier().getMinScore();
                            break;
                        case NONE:
                            reqRank = 1;
                            curRank = 0;
                            break;
                    }

                    user.getSidebar().clear();
                    user.getSidebar().setName("§b§n" + player.getName());
                    user.getSidebar().setAll(
                            "§f",
                            "§e보유 중인 돈",
                            "§6" + String.format("%,d", userData.getMoney()),
                            "§f§f",
                            "§f레벨 : " + StringFormUtil.getLevelPrefix(userData.getLevel()),
                            StringFormUtil.getProgressBar(userData.getXp(), reqXp, ChatColor.DARK_GREEN) + " §2[" + userData.getXp() + "/" + reqXp + "]",
                            "§f§f§f",
                            "§f랭크 : " + userData.getTier().getPrefix(),
                            StringFormUtil.getProgressBar(rank - curRank, reqRank - curRank, ChatColor.DARK_AQUA) + " §3[" + rank + "/" + reqRank + "]"
                    );
                }

                return true;
            }
        });
    }
}
