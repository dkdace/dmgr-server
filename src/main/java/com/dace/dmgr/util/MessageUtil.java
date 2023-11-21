package com.dace.dmgr.util;

import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.SystemPrefix;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

/**
 * 메시지, 액션바, 타이틀 등 메시지 전송 기능을 제공하는 클래스.
 */
public final class MessageUtil {
    /**
     * 플레이어의 채팅창을 청소한다.
     *
     * @param player 대상 플레이어
     */
    public static void clearChat(Player player) {
        for (int i = 0; i < 100; i++) {
            player.sendMessage("§f");
        }
    }

    /**
     * 플레이어의 채팅창에 일반 메시지를 전송한다.
     *
     * @param player  대상 플레이어
     * @param message 메시지
     */
    public static void sendMessage(Player player, String message) {
        message = message.replace("\n", "\n" + SystemPrefix.CHAT);
        player.sendMessage(SystemPrefix.CHAT + message);
    }

    /**
     * 플레이어의 채팅창에 경고 메시지를 전송한다.
     *
     * @param player  대상 플레이어
     * @param message 메시지
     */
    public static void sendMessageWarn(Player player, String message) {
        message = message.replace("\n", "\n" + SystemPrefix.CHAT_WARN);
        player.sendMessage(SystemPrefix.CHAT_WARN + message);
    }

    /**
     * 플레이어에게 액션바를 전송한다.
     *
     * @param player        대상 플레이어
     * @param message       메시지
     * @param overrideTicks 덮어쓰기 지속시간 (tick). {@code 0} 이상으로 지정하면 지속시간 동안 기존 액션바 출력을 무시한다.
     */
    public static void sendActionBar(Player player, String message, long overrideTicks) {
        if (overrideTicks > 0)
            CooldownManager.setCooldown(player, Cooldown.ACTION_BAR, overrideTicks);
        else if (CooldownManager.getCooldown(player, Cooldown.ACTION_BAR) > 0)
            return;

        TextComponent actionBar = new TextComponent(message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBar);
    }

    /**
     * 플레이어에게 액션바를 전송한다.
     *
     * @param message 메시지
     */
    public static void sendActionBar(Player player, String message) {
        sendActionBar(player, message, 0);
    }

    /**
     * 플레이어에게 타이틀을 전송한다.
     *
     * @param player        대상 플레이어
     * @param title         제목
     * @param subtitle      부제목
     * @param fadeIn        나타나는 시간 (tick)
     * @param stay          유지 시간 (tick)
     * @param fadeOut       사라지는 시간 (tick)
     * @param overrideTicks 덮어쓰기 지속시간 (tick). {@code 0} 이상으로 지정하면 지속시간 동안 기존 타이틀 출력을 무시한다.
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut, long overrideTicks) {
        if (overrideTicks > 0)
            CooldownManager.setCooldown(player, Cooldown.TITLE, overrideTicks);
        else if (CooldownManager.getCooldown(player, Cooldown.TITLE) > 0)
            return;

        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * 플레이어에게 타이틀을 전송한다.
     *
     * @param player   대상 플레이어
     * @param title    제목
     * @param subtitle 부제목
     * @param fadeIn   나타나는 시간 (tick)
     * @param stay     유지 시간 (tick)
     * @param fadeOut  사라지는 시간 (tick)
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        sendTitle(player, title, subtitle, fadeIn, stay, fadeOut, 0);
    }
}
