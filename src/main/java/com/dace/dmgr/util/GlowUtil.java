package com.dace.dmgr.util;

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.WeakHashMap;

/**
 * 발광 효과 재생 기능을 제공하는 클래스.
 */
@UtilityClass
public final class GlowUtil {
    /** 플레이어별 발광 엔티티 목록. (플레이어 : (발광 엔티티 : 색상 번호)) */
    private final WeakHashMap<Player, WeakHashMap<Entity, Integer>> glowingMap = new WeakHashMap<>();

    /**
     * 지정한 엔티티에게 특정 플레이어만 보이는 발광 효과를 적용한다.
     *
     * @param entity 발광 효과를 적용할 엔티티
     * @param color  색상
     * @param player 대상 플레이어
     */
    public static void setGlowing(@NonNull Entity entity, @NonNull ChatColor color, @NonNull Player player) {
        glowingMap.putIfAbsent(player, new WeakHashMap<>());
        WeakHashMap<Entity, Integer> glowings = glowingMap.get(player);
        int colorValue = glowings.getOrDefault(entity, -1);
        if (colorValue == color.ordinal())
            return;

        WrapperPlayServerScoreboardTeam packet1 = new WrapperPlayServerScoreboardTeam();
        WrapperPlayServerScoreboardTeam packet2 = new WrapperPlayServerScoreboardTeam();
        WrapperPlayServerScoreboardTeam packet3 = new WrapperPlayServerScoreboardTeam();
        WrapperPlayServerScoreboardTeam packet4 = new WrapperPlayServerScoreboardTeam();
        packet1.setName("Glow" + color.ordinal());
        packet2.setName("Glow" + color.ordinal());
        packet3.setName("Glow" + color.ordinal());
        packet4.setName("Glow" + colorValue);

        packet1.setMode(0);

        packet2.setMode(2);
        packet2.setNameTagVisibility("never");
        packet2.setCollisionRule("never");
        packet2.setPrefix(color + "");
        packet2.setColor(color.ordinal());

        packet3.setMode(3);
        packet3.setPlayers(Arrays.asList(entity instanceof Player ? entity.getName() : entity.getUniqueId().toString()));

        packet4.setMode(4);
        packet4.setPlayers(Arrays.asList(entity instanceof Player ? entity.getName() : entity.getUniqueId().toString()));

        packet1.sendPacket(player);
        packet2.sendPacket(player);
        packet3.sendPacket(player);
        packet4.sendPacket(player);

        if (glowings.put(entity, color.ordinal()) == null)
            runGlowingPacket(entity, player);
    }

    /**
     * 발광 패킷 전송 스케쥴러를 실행한다.
     *
     * @param entity 발광 효과 적용 엔티티
     * @param player 대상 플레이어
     */
    private static void runGlowingPacket(@NotNull Entity entity, @NotNull Player player) {
        new IntervalTask(i -> {
            if (!entity.isValid() || !player.isValid())
                return false;

            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata();

            packet.setEntityID(entity.getEntityId());
            WrappedDataWatcher dw = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
            if (isGlowing(entity, player))
                dw.setObject(0, (byte) (dw.getByte(0) | (1 << 6)));
            else
                dw.setObject(0, (byte) (dw.getByte(0) & ~(1 << 6)));
            packet.setMetadata(dw.getWatchableObjects());

            packet.sendPacket(player);

            return isGlowing(entity, player);
        }, 1);
    }

    /**
     * 지정한 엔티티가 특정 플레이어에게 발광 상태인 지 확인한다.
     *
     * @param entity 확인할 엔티티
     * @param player 대상 플레이어
     * @return 대상 플레이어에게 발광 상태면 {@code true} 반환
     */
    public static boolean isGlowing(@NonNull Entity entity, @NonNull Player player) {
        WeakHashMap<Entity, Integer> glowings = glowingMap.get(player);
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
        WeakHashMap<Entity, Integer> glowings = glowingMap.get(player);
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
    }
}
