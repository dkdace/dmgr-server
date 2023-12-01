package com.dace.dmgr.util;

import com.comphenix.packetwrapper.WrapperPlayServerBoss;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 플레이어별 보스바 생성 및 관리 기능을 제공하는 클래스.
 */
public final class BossBarUtil {
    /** 플레이어별 생성된 보스바 UUID 목록 (플레이어 : (보스바 ID : UUID)) */
    private static final Map<Player, Map<String, UUID>> playerBossBarMap = new HashMap<>();

    /**
     * 지정한 플레이어에게 보스바를 표시한다.
     *
     * <p>이미 해당 ID의 보스바가 존재할 경우 덮어쓴다.</p>
     *
     * @param player   대상 플레이어
     * @param id       보스바 ID
     * @param message  내용
     * @param color    막대 색
     * @param style    막대 스타일
     * @param progress 진행률. {@code 0 ~ 1} 사이의 값
     */
    public static void addBossBar(Player player, String id, String message, BarColor color, WrapperPlayServerBoss.BarStyle style, float progress) {
        playerBossBarMap.putIfAbsent(player, new HashMap<>());

        Map<String, UUID> bossBarMap = playerBossBarMap.get(player);
        UUID uuid = bossBarMap.getOrDefault(id, UUID.randomUUID());

        if (bossBarMap.get(id) == null) {
            WrapperPlayServerBoss packet = new WrapperPlayServerBoss();

            packet.setUniqueId(uuid);
            packet.setAction(WrapperPlayServerBoss.Action.ADD);
            packet.setTitle(WrappedChatComponent.fromText(message));
            packet.setColor(color);
            packet.setStyle(style);
            packet.setHealth(progress);

            packet.sendPacket(player);

            bossBarMap.put(id, uuid);
        } else {
            WrapperPlayServerBoss packet1 = new WrapperPlayServerBoss();
            WrapperPlayServerBoss packet2 = new WrapperPlayServerBoss();
            WrapperPlayServerBoss packet3 = new WrapperPlayServerBoss();
            packet1.setUniqueId(uuid);
            packet2.setUniqueId(uuid);
            packet3.setUniqueId(uuid);

            packet1.setAction(WrapperPlayServerBoss.Action.UPDATE_NAME);
            packet1.setTitle(WrappedChatComponent.fromText(message));
            packet2.setAction(WrapperPlayServerBoss.Action.UPDATE_STYLE);
            packet2.setColor(color);
            packet2.setStyle(style);
            packet3.setAction(WrapperPlayServerBoss.Action.UPDATE_PCT);
            packet3.setHealth(progress);

            packet1.sendPacket(player);
            packet2.sendPacket(player);
            packet3.sendPacket(player);
        }
    }

    /**
     * 지정한 플레이어의 보스바를 제거한다.
     *
     * @param player 대상 플레이어
     * @param id     보스바 ID
     */
    public static void removeBossBar(Player player, String id) {
        Map<String, UUID> bossBarMap = playerBossBarMap.get(player);
        if (bossBarMap == null)
            return;
        UUID uuid = bossBarMap.get(id);
        if (uuid == null)
            return;

        WrapperPlayServerBoss packet = new WrapperPlayServerBoss();

        packet.setAction(WrapperPlayServerBoss.Action.REMOVE);
        packet.setUniqueId(uuid);

        packet.sendPacket(player);

        bossBarMap.remove(id, uuid);
    }

    /**
     * 지정한 플레이어의 모든 보스바를 제거한다.
     *
     * @param player 대상 플레이어
     */
    public static void clearBossBar(Player player) {
        Map<String, UUID> bossBarMap = playerBossBarMap.get(player);
        if (bossBarMap == null)
            return;

        bossBarMap.forEach((id, uuid) -> {
            WrapperPlayServerBoss packet = new WrapperPlayServerBoss();

            packet.setAction(WrapperPlayServerBoss.Action.REMOVE);
            packet.setUniqueId(uuid);

            packet.sendPacket(player);
        });

        playerBossBarMap.remove(player);
    }
}
