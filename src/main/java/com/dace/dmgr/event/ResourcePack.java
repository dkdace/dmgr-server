package com.dace.dmgr.event;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.user.User;
import com.dace.dmgr.config.GeneralConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static com.dace.dmgr.system.EntityList.userList;

public class ResourcePack {
    private static final String DENY_KICK_MESSAGE = "§3§l[ §b§lDMGR §3§l] §6리소스팩 적용을 활성화 해주세요!" +
            "\n§e멀티플레이 → 편집 → 서버 리소스 팩 : 사용" +
            "\n" +
            "\n§f다운로드가 되지 않으면, .minecraft → server-resource-packs 폴더를 생성하세요." +
            "\n" +
            "\n§7다운로드 오류 문의 : 디스코드 DarkDace＃4671";
    private static final String ERR_KICK_MESSAGE = "§3§l[ §b§lDMGR §3§l] §c리소스팩 적용 중 오류가 발생했습니다." +
            "\n§f잠시 후 다시 시도하거나, 게임을 재부팅 하십시오." +
            "\n" +
            "\n§7다운로드 오류 문의 : 디스코드 DarkDace＃4671";

    public static void event(PlayerResourcePackStatusEvent event, Player player) {
        User user = userList.get(player.getUniqueId());

        user.resourcePackStatus = event.getStatus();

        if (user.resourcePackStatus == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            player.kickPlayer(ERR_KICK_MESSAGE);
        }
    }

    public static void sendResourcePack(User user) {
        if (!user.resourcePack) {
            user.resourcePack = true;
            user.getPlayer().setResourcePack(GeneralConfig.resourcePackUrl);

            new BukkitRunnable() {
                int i = 8 * 20;

                public void run() {
                    if (i-- <= 0 || !user.getPlayer().isOnline()) cancel();

                    if (user.resourcePackStatus != null)
                        cancel();

                    if (isCancelled()) {
                        if (user.resourcePackStatus == null || user.resourcePackStatus == PlayerResourcePackStatusEvent.Status.DECLINED)
                            user.getPlayer().kickPlayer(DENY_KICK_MESSAGE);
                    }
                }
            }.runTaskTimer(DMGR.getPlugin(), 20, 1);
        }
    }
}
