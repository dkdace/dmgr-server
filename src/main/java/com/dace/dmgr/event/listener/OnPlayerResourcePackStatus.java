package com.dace.dmgr.event.listener;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.config.GeneralConfig;
import com.dace.dmgr.system.task.TaskWait;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import static com.dace.dmgr.system.HashMapList.userMap;

public class OnPlayerResourcePackStatus implements Listener {
    private static final String DENY_KICK_MESSAGE = DMGR.PREFIX.CHAT_WARN + "리소스팩 적용을 활성화 해주세요!" +
            "\n§e멀티플레이 → 편집 → 서버 리소스 팩 : 사용" +
            "\n" +
            "\n§f다운로드가 되지 않으면, .minecraft → server-resource-packs 폴더를 생성하세요." +
            "\n" +
            "\n§7다운로드 오류 문의 : 디스코드 DarkDace＃4671";
    private static final String ERR_KICK_MESSAGE = DMGR.PREFIX.CHAT_WARN + "리소스팩 적용 중 오류가 발생했습니다." +
            "\n§f잠시 후 다시 시도하거나, 게임을 재부팅 하십시오." +
            "\n" +
            "\n§7다운로드 오류 문의 : 디스코드 DarkDace＃4671";

    @EventHandler
    public static void event(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        User user = userMap.get(player);

        user.setResourcePackStatus(event.getStatus());

        if (user.getResourcePackStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            player.kickPlayer(ERR_KICK_MESSAGE);
        }
    }

    public static void sendResourcePack(Player player) {
        User user = userMap.get(player);

        if (!user.isResourcePack()) {
            user.setResourcePack(true);
            user.getPlayer().setResourcePack(GeneralConfig.resourcePackUrl);

            new TaskWait(160) {
                @Override
                public void run() {
                    if (player.isOnline())
                        if (user.getResourcePackStatus() == null || user.getResourcePackStatus() == PlayerResourcePackStatusEvent.Status.DECLINED)
                            user.getPlayer().kickPlayer(DENY_KICK_MESSAGE);
                }
            };
        }
    }
}
