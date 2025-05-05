package com.dace.dmgr.user;

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.WeakHashMap;

/**
 * 플레이어의 발광 효과 상태를 관리하는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class GlowingManager {
    /** 발광 효과 정보 목록 (발광 엔티티 : 발광 효과 정보) */
    private final WeakHashMap<Entity, GlowingInfo> glowingInfoMap = new WeakHashMap<>();
    /** 유저 인스턴스 */
    private final User user;

    /**
     * 플레이어에게 지정한 엔티티를 지속시간동안 발광 상태로 표시한다.
     *
     * @param target   발광 효과를 적용할 엔티티
     * @param color    색상
     * @param duration 지속시간
     */
    public void setGlowing(@NonNull Entity target, @NonNull ChatColor color, @NonNull Timespan duration) {
        glowingInfoMap.computeIfAbsent(target, k -> new GlowingInfo(target)).set(color, duration);
    }

    /**
     * 플레이어에게 지정한 엔티티를 발광 상태로 표시한다.
     *
     * @param target 발광 효과를 적용할 엔티티
     * @param color  색상
     */
    public void setGlowing(@NonNull Entity target, @NonNull ChatColor color) {
        setGlowing(target, color, Timespan.MAX);
    }

    /**
     * 지정한 엔티티가 발광 상태인지 확인한다.
     *
     * @param target 확인할 엔티티
     * @return 플레이어에게 발광 상태면 {@code true} 반환
     */
    public boolean isGlowing(@NonNull Entity target) {
        GlowingInfo glowingInfo = glowingInfoMap.get(target);
        return glowingInfo != null && glowingInfo.expiration.isAfter(Timestamp.now());
    }

    /**
     * 지정한 엔티티의 발광 효과를 제거한다.
     *
     * @param target 발광 효과가 적용된 엔티티
     */
    public void removeGlowing(@NonNull Entity target) {
        GlowingInfo glowingInfo = glowingInfoMap.remove(target);
        if (glowingInfo != null)
            glowingInfo.remove();
    }

    /**
     * 플레이어에게 표시되는 모든 발광 효과를 제거한다.
     */
    public void clearGlowing() {
        new HashSet<>(glowingInfoMap.keySet()).forEach(this::removeGlowing);
    }

    /**
     * 발광 효과 정보 클래스.
     */
    private final class GlowingInfo {
        /** 플레이어 */
        private final Player player;
        /** 발광 효과 적용 엔티티 */
        private final Entity target;
        /** 틱 작업을 처리하는 태스크 */
        private final IntervalTask onTickTask;
        /** 색상 */
        private ChatColor color;
        /** 종료 시점 */
        private Timestamp expiration = Timestamp.now();

        private GlowingInfo(@NonNull Entity target) {
            this.player = user.getPlayer();
            this.target = target;
            this.onTickTask = new IntervalTask(i -> {
                if (!target.isValid() || expiration.isBefore(Timestamp.now()))
                    return false;

                if (i % 4 == 0)
                    sendGlowingPacket(true);

                return true;
            }, () -> removeGlowing(target), 1);
        }

        /**
         * 엔티티의 발광 효과의 색상과 지속시간을 설정한다.
         *
         * @param color    색상
         * @param duration 지속시간
         */
        private void set(@NonNull ChatColor color, @NonNull Timespan duration) {
            this.color = color;

            sendRemoveTeamPacket();
            sendAddTeamPacket();

            if (expiration.isBefore(Timestamp.now()) || duration.compareTo(Timestamp.now().until(expiration)) > 0)
                expiration = Timestamp.now().plus(duration);
        }

        /**
         * 엔티티의 발광 효과를 제거한다.
         */
        public void remove() {
            sendRemoveTeamPacket();
            sendGlowingPacket(false);

            onTickTask.stop();
        }

        /**
         * 플레이어에게 발광 효과 패킷을 전송한다.
         *
         * @param isEnabled 활성화 여부
         */
        private void sendGlowingPacket(boolean isEnabled) {
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata();

            packet.setEntityID(target.getEntityId());

            WrappedDataWatcher dw = WrappedDataWatcher.getEntityWatcher(target).deepClone();
            dw.setObject(0, (byte) (isEnabled ? dw.getByte(0) | 1 << 6 : dw.getByte(0) & ~(1 << 6)));
            packet.setMetadata(dw.getWatchableObjects());

            packet.sendPacket(player);
        }

        /**
         * 플레이어에게 팀 추가 패킷을 전송한다.
         */
        private void sendAddTeamPacket() {
            WrapperPlayServerScoreboardTeam[] packets = new WrapperPlayServerScoreboardTeam[3];
            Arrays.setAll(packets, i -> createScoreboardTeamPacket());

            packets[0].setMode(0);

            packets[1].setMode(2);
            packets[1].setNameTagVisibility("never");
            packets[1].setCollisionRule("never");
            packets[1].setPrefix(color.toString());
            packets[1].setColor(color.ordinal());

            packets[2].setMode(3);

            for (WrapperPlayServerScoreboardTeam packet : packets)
                packet.sendPacket(player);
        }

        /**
         * 플레이어에게 팀 제거 패킷을 전송한다.
         */
        private void sendRemoveTeamPacket() {
            WrapperPlayServerScoreboardTeam packet = createScoreboardTeamPacket();

            packet.setMode(4);
            packet.sendPacket(player);
        }

        @NonNull
        private WrapperPlayServerScoreboardTeam createScoreboardTeamPacket() {
            WrapperPlayServerScoreboardTeam packet = new WrapperPlayServerScoreboardTeam();

            packet.setName("Glowing" + color.ordinal());
            packet.setPlayers(Collections.singletonList(target instanceof Player ? target.getName() : target.getUniqueId().toString()));

            return packet;
        }
    }
}
