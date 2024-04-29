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

import java.util.Arrays;

/**
 * 발광 효과 재생 기능을 제공하는 클래스.
 */
@UtilityClass
public final class GlowUtil {
    /** 쿨타임 ID */
    private static final String COOLDOWN_ID = "Glow";

    /**
     * 지정한 엔티티에게 지속시간동안 특정 플레이어만 보이는 발광 효과를 적용한다.
     *
     * @param entity   발광 효과를 적용할 엔티티
     * @param color    색상
     * @param player   대상 플레이어
     * @param duration 지속시간 (tick). -1로 설정 시 무한 지속
     */
    public static void setGlowing(@NonNull Entity entity, @NonNull ChatColor color, @NonNull Player player, long duration) {
        if (duration == -1)
            duration = Integer.MAX_VALUE;

        sendAddTeamPacket(entity, color, player);

        String id = COOLDOWN_ID + player;
        if (CooldownUtil.getCooldown(entity, Cooldown.STATUS_EFFECT, id) == 0) {
            CooldownUtil.setCooldown(entity, Cooldown.STATUS_EFFECT, id, duration);

            new IntervalTask(i -> {
                if (!entity.isValid() || !player.isValid() || CooldownUtil.getCooldown(entity, Cooldown.STATUS_EFFECT, id) == 0)
                    return false;

                sendGlowingPakcet(entity, player, true);

                return true;
            }, isCancelled -> {
                sendGlowingPakcet(entity, player, false);
                sendRemoveTeamPacket(entity, player);
            }, 1);
        } else if (CooldownUtil.getCooldown(entity, Cooldown.STATUS_EFFECT, id) < duration)
            CooldownUtil.setCooldown(entity, Cooldown.STATUS_EFFECT, id, duration);
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
     * 지정한 엔티티가 특정 플레이어에게 발광 상태인 지 확인한다.
     *
     * @param entity 확인할 엔티티
     * @param player 대상 플레이어
     * @return 대상 플레이어에게 발광 상태면 {@code true} 반환
     */
    public static boolean isGlowing(@NonNull Entity entity, @NonNull Player player) {
        return CooldownUtil.getCooldown(entity, Cooldown.STATUS_EFFECT, COOLDOWN_ID + player) > 0;
    }

    /**
     * 지정한 엔티티의 발광 효과를 제거한다.
     *
     * @param entity 발광 효과가 적용된 엔티티
     * @param player 대상 플레이어
     */
    public static void removeGlowing(@NonNull Entity entity, @NonNull Player player) {
        CooldownUtil.setCooldown(entity, Cooldown.STATUS_EFFECT, COOLDOWN_ID + player, 0);
    }

    /**
     * 지정한 엔티티에게 발광 효과 패킷을 전송한다.
     *
     * @param isEnabled 활성화 여부
     */
    private static void sendGlowingPakcet(@NonNull Entity entity, @NonNull Player player, boolean isEnabled) {
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
//        sendRemoveTeamPacket(entity, player);

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
        packet3.setPlayers(Arrays.asList(entity instanceof Player ? entity.getName() : entity.getUniqueId().toString()));

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
        for (ChatColor chatColor : ChatColor.values()) {
            WrapperPlayServerScoreboardTeam packet = new WrapperPlayServerScoreboardTeam();

            packet.setMode(4);
            packet.setName(COOLDOWN_ID + chatColor.ordinal());
            packet.setPlayers(Arrays.asList(entity instanceof Player ? entity.getName() : entity.getUniqueId().toString()));

            packet.sendPacket(player);
        }
    }
}
