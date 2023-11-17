package com.dace.dmgr.event.listener;

import com.dace.dmgr.system.GeneralConfig;
import com.dace.dmgr.lobby.User;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.SystemPrefix;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskWait;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public final class OnPlayerResourcePackStatus implements Listener {
    /** 리소스팩 미적용으로 강제퇴장 시 표시되는 메시지 */
    private static final String DENY_KICK_MESSAGE = SystemPrefix.CHAT_WARN + "리소스팩 적용을 활성화 해주세요!" +
            "\n§e멀티플레이 → 편집 → 서버 리소스 팩 : 사용" +
            "\n" +
            "\n§f다운로드가 되지 않으면, .minecraft → server-resource-packs 폴더를 생성하세요." +
            "\n" +
            "\n§7다운로드 오류 문의 : 디스코드 DarkDace＃4671";
    /** 리소스팩 오류로 강제퇴장 시 표시되는 메시지 */
    private static final String ERR_KICK_MESSAGE = SystemPrefix.CHAT_WARN + "리소스팩 적용 중 오류가 발생했습니다." +
            "\n§f잠시 후 다시 시도하거나, 게임을 재부팅 하십시오." +
            "\n" +
            "\n§7다운로드 오류 문의 : 디스코드 DarkDace＃4671";

    @EventHandler
    public static void event(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        User user = EntityInfoRegistry.getUser(player);

        user.setResourcePackStatus(event.getStatus());

        if (user.getResourcePackStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            player.kickPlayer(ERR_KICK_MESSAGE);
        }
    }

    /**
     * 플레이어에게 리소스팩을 전송하고, 적용하지 않을 시 강제 퇴장 시킨다.
     *
     * @param player 대상 플레이어
     */
    public static void sendResourcePack(Player player) {
        User user = EntityInfoRegistry.getUser(player);

        if (!user.isResourcePack()) {
            user.setResourcePack(true);
            user.getPlayer().setResourcePack(GeneralConfig.RESOURCE_PACK_URL);

            TaskManager.addTask(user, new TaskWait(160) {
                @Override
                public void onEnd() {
                    if (user.getResourcePackStatus() == null || user.getResourcePackStatus() == PlayerResourcePackStatusEvent.Status.DECLINED)
                        user.getPlayer().kickPlayer(DENY_KICK_MESSAGE);
                }
            });
        }
    }
}
