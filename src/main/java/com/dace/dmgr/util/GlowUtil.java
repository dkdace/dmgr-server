package com.dace.dmgr.util;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * 발광 효과 재생 기능을 제공하는 클래스.
 */
@UtilityClass
public final class GlowUtil {
    /** 플레이어별 발광 엔티티 목록. (플레이어 : (발광 엔티티 : 색상 번호)) */
    private final WeakHashMap<Player, HashMap<Entity, Integer>> glowingMap = new WeakHashMap<>();

    /**
     * 지정한 엔티티에게 특정 플레이어만 보이는 발광 효과를 적용한다.
     *
     * @param entity 발광 효과를 적용할 엔티티
     * @param color  색상
     * @param player 대상 플레이어
     */
    public static void setGlowing(@NonNull Entity entity, @NonNull ChatColor color, @NonNull Player player) {
        glowingMap.putIfAbsent(player, new HashMap<>());
        HashMap<Entity, Integer> glowings = glowingMap.get(player);
        int colorValue = glowings.getOrDefault(entity, -1);
        if (colorValue == color.ordinal())
            return;

        removeGlowing(entity, player);
        glowings.putIfAbsent(entity, color.ordinal());

        WrapperPlayServerScoreboardTeam packet1 = new WrapperPlayServerScoreboardTeam();
        WrapperPlayServerScoreboardTeam packet2 = new WrapperPlayServerScoreboardTeam();
        WrapperPlayServerScoreboardTeam packet3 = new WrapperPlayServerScoreboardTeam();
        packet1.setName("Glow" + color.ordinal());
        packet2.setName("Glow" + color.ordinal());
        packet3.setName("Glow" + color.ordinal());

        packet1.setMode(0);

        packet2.setMode(2);
        packet2.setNameTagVisibility("always");
        packet2.setCollisionRule("never");
        packet2.setPrefix(color + "");
        packet2.setColor(color.ordinal());

        packet3.setMode(3);
        packet3.setPlayers(Arrays.asList(entity instanceof Player ? entity.getName() : entity.getUniqueId().toString()));

        packet1.sendPacket(player);
        packet2.sendPacket(player);
        packet3.sendPacket(player);

        entity.setGlowing(true);
        entity.setGlowing(false);
    }

    /**
     * 지정한 엔티티가 특정 플레이어에게 발광 상태인 지 확인한다.
     *
     * @param entity 확인할 엔티티
     * @param player 대상 플레이어
     * @return 대상 플레이어에게 발광 상태면 {@code true} 반환
     */
    public static boolean isGlowing(@NonNull Entity entity, @NonNull Player player) {
        HashMap<Entity, Integer> glowings = glowingMap.get(player);
        if (glowings == null)
            return false;

        return glowings.get(entity) != null;
    }

    /**
     * 지정한 엔티티의 발광 효과를 제거한다.
     *
     * @param entity 발광 효과가 적용된 엔티티
     * @param player 대상 플레이어
     */
    public static void removeGlowing(@NonNull Entity entity, @NonNull Player player) {
        HashMap<Entity, Integer> glowings = glowingMap.get(player);
        if (glowings == null)
            return;
        int colorValue = glowings.getOrDefault(entity, -1);
        if (colorValue == -1)
            return;

        WrapperPlayServerScoreboardTeam packet = new WrapperPlayServerScoreboardTeam();

        packet.setMode(4);
        packet.setName("Glow" + colorValue);
        packet.setPlayers(Arrays.asList(entity instanceof Player ? entity.getName() : entity.getUniqueId().toString()));

        packet.sendPacket(player);

        glowings.remove(entity);

        entity.setGlowing(true);
        entity.setGlowing(false);
    }
}
