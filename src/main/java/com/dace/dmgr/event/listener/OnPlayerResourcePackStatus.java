package com.dace.dmgr.event.listener;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public final class OnPlayerResourcePackStatus implements Listener {
    /** 리소스팩 미적용으로 강제퇴장 시 표시되는 메시지 */
    public static final String MESSAGE_KICK_DENY = "§c리소스팩 적용을 활성화 하십시오." +
            "\n" +
            "\n§e멀티플레이 → 편집 → 서버 리소스 팩 : 사용" +
            "\n" +
            "\n§f다운로드가 되지 않으면, .minecraft → server-resource-packs 폴더를 생성하십시오." +
            "\n" +
            "\n§7다운로드 오류 문의 : " + GeneralConfig.getConfig().getAdminContact();
    /** 리소스팩 오류로 강제퇴장 시 표시되는 메시지 */
    public static final String MESSAGE_KICK_ERR = "§c리소스팩 적용 중 오류가 발생했습니다." +
            "\n" +
            "\n§f잠시 후 다시 시도하거나, 게임을 재부팅 하십시오." +
            "\n" +
            "\n§7다운로드 오류 문의 : " + GeneralConfig.getConfig().getAdminContact();

    @EventHandler
    public static void event(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        User user = User.fromPlayer(player);

        user.setResourcePackAccepted(event.getStatus() == PlayerResourcePackStatusEvent.Status.ACCEPTED ||
                event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED);

        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)
            player.kickPlayer(GeneralConfig.getConfig().getMessagePrefix() + MESSAGE_KICK_ERR);
    }
}
