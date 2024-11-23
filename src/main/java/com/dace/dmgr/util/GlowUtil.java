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

import java.util.Collections;
import java.util.WeakHashMap;

/**
 * 발광 효과 재생 기능을 제공하는 클래스.
 */
@UtilityClass
public final class GlowUtil {
    /** 플레이어별 발광 엔티티 목록. (플레이어 : (발광 엔티티 : 색상)) */
    private final WeakHashMap<Player, WeakHashMap<Entity, ChatColor>> glowingMap = new WeakHashMap<>();
    /** 쿨타임 ID */
    private static final String COOLDOWN_ID = "Glow";

    /**
     * 지정한 엔티티에게 지속시간동안 특정 플레이어만 보이는 발광 효과를 적용한다.
     *
     * @param entity   발광 효과를 적용할 엔티티
     * @param color    색상
     * @param player   대상 플레이어
     * @param duration 지속시간 (tick). -1로 설정 시 무한 지속
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void setGlowing(@NonNull Entity entity, @NonNull ChatColor color, @NonNull Player player, long duration) {
        if (duration < -1)
            throw new IllegalArgumentException("'duration'이 -1 이상이어야 함");
        if (duration == -1)
            duration = Long.MAX_VALUE;

        sendAddTeamPacket(entity, color, player);

        if (CooldownUtil.getCooldown(entity, COOLDOWN_ID + player) == 0) {
            CooldownUtil.setCooldown(entity, COOLDOWN_ID + player, duration);

            new IntervalTask(i -> {
                if (!entity.isValid() || !player.isValid() || CooldownUtil.getCooldown(entity, COOLDOWN_ID + player) == 0)
                    return false;

                sendGlowingPacket(entity, player, true);

                return true;
            }, () -> {
                sendGlowingPacket(entity, player, false);
                sendRemoveTeamPacket(entity, player);
            }, 1);
        } else if (CooldownUtil.getCooldown(entity, COOLDOWN_ID + player) < duration)
            CooldownUtil.setCooldown(entity, COOLDOWN_ID + player, duration);
    }

    /**
     * 지정한 엔티티에게 특정 플레이어만 보이는 발광 효과를 적용한다.
     *
     * @param entity 발광 효과를 적용할 엔티티
     * @param color  색상
     * @param player 대상 플레이어
     */
    public static void setGlowing(@NonNull Entity entity, @NonNull ChatColor color, @NonNull Player player) {
        setGlowing(entity, color, player, -1);
    }

    /**
     * 지정한 엔티티가 특정 플레이어에게 발광 상태인지 확인한다.
     *
     * @param entity 확인할 엔티티
     * @param player 대상 플레이어
     * @return 대상 플레이어에게 발광 상태면 {@code true} 반환
     */
    public static boolean isGlowing(@NonNull Entity entity, @NonNull Player player) {
        return CooldownUtil.getCooldown(entity, COOLDOWN_ID + player) > 0;
    }

    /**
     * 지정한 엔티티의 발광 효과를 제거한다.
     *
     * @param entity 발광 효과가 적용된 엔티티
     * @param player 대상 플레이어
     */
    public static void removeGlowing(@NonNull Entity entity, @NonNull Player player) {
        CooldownUtil.setCooldown(entity, COOLDOWN_ID + player, 0);
    }

    /**
     * 지정한 엔티티에게 발광 효과 패킷을 전송한다.
     *
     * @param isEnabled 활성화 여부
     */
    private static void sendGlowingPacket(@NonNull Entity entity, @NonNull Player player, boolean isEnabled) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata();

        packet.setEntityID(entity.getEntityId());
        WrappedDataWatcher dw = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
        dw.setObject(0, isEnabled ? (byte) (dw.getByte(0) | (1 << 6)) : (byte) (dw.getByte(0) & ~(1 << 6)));
        packet.setMetadata(dw.getWatchableObjects());

        packet.sendPacket(player);
    }

    /**
     * 지정한 엔티티에게 팀 추가 패킷을 전송한다.
     *
     * @param entity 발광 효과를 적용할 엔티티
     * @param color  색상
     * @param player 대상 플레이어
     */
    private static void sendAddTeamPacket(@NonNull Entity entity, @NonNull ChatColor color, @NonNull Player player) {
        glowingMap.putIfAbsent(player, new WeakHashMap<>());
        sendRemoveTeamPacket(entity, player);
        WeakHashMap<Entity, ChatColor> glowings = glowingMap.get(player);

        glowings.put(entity, color);

        WrapperPlayServerScoreboardTeam packet1 = new WrapperPlayServerScoreboardTeam();
        WrapperPlayServerScoreboardTeam packet2 = new WrapperPlayServerScoreboardTeam();
        WrapperPlayServerScoreboardTeam packet3 = new WrapperPlayServerScoreboardTeam();
        packet1.setName(COOLDOWN_ID + color.ordinal());
        packet2.setName(COOLDOWN_ID + color.ordinal());
        packet3.setName(COOLDOWN_ID + color.ordinal());

        packet1.setMode(0);

        packet2.setMode(2);
        packet2.setNameTagVisibility("never");
        packet2.setCollisionRule("never");
        packet2.setPrefix(color + "");
        packet2.setColor(color.ordinal());

        packet3.setMode(3);
        packet3.setPlayers(Collections.singletonList(entity instanceof Player ? entity.getName() : entity.getUniqueId().toString()));

        packet1.sendPacket(player);
        packet2.sendPacket(player);
        packet3.sendPacket(player);
    }

    /**
     * 지정한 엔티티에게 팀 제거 패킷을 전송한다.
     *
     * @param entity 발광 효과가 적용된 엔티티
     * @param player 대상 플레이어
     */
    private static void sendRemoveTeamPacket(@NonNull Entity entity, @NonNull Player player) {
        WeakHashMap<Entity, ChatColor> glowings = glowingMap.get(player);
        ChatColor color = glowings.get(entity);
        if (color == null)
            return;

        glowings.remove(entity);

        WrapperPlayServerScoreboardTeam packet = new WrapperPlayServerScoreboardTeam();

        packet.setMode(4);
        packet.setName(COOLDOWN_ID + color.ordinal());
        packet.setPlayers(Collections.singletonList(entity instanceof Player ? entity.getName() : entity.getUniqueId().toString()));

        packet.sendPacket(player);
    }
}
