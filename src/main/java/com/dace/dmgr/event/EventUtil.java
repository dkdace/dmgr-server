package com.dace.dmgr.event;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.dace.dmgr.DMGR;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.event.Listener;

/**
 * 이벤트 등록 기능을 제공하는 클래스.
 *
 * <p>이벤트는 반드시 플러그인이 활성화될 때 등록해야 한다.</p>
 */
@UtilityClass
public final class EventUtil {
    /** 패킷 이벤트 등록을 위한 객체 */
    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    /**
     * 일반 이벤트를 등록한다.
     *
     * <p>이벤트 처리기는 {@link Listener}를 상속받는 클래스여야 하며, 다음과 같은
     * 형태로 구현되어야 한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     *
     * public class OnPlayerJoin implements Listener {
     *     // 이벤트가 발동할 때 호출된다.
     *     // 대상 이벤트(event)는 Event를 상속받는 클래스이다.
     *     @EventHandler
     *     public static void event(PlayerJoinEvent event) {
     *         Player player = event.getPlayer();
     *     }
     * }
     *
     * }</pre>
     *
     * @param listener 이벤트 처리기
     */
    public static void registerListener(@NonNull Listener listener) {
        DMGR.getPlugin().getServer().getPluginManager().registerEvents(listener, DMGR.getPlugin());
    }

    /**
     * 패킷 이벤트를 등록한다.
     *
     * <p>패킷 이벤트 처리기는 {@link PacketAdapter}를 상속받는 클래스여야 하며,
     * 다음과 같은 형태로 구현해야 한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     *
     * public class OnPlayServerUpdateHealth extends PacketAdapter {
     *     // 생성자에서 패킷 타입(PacketType)을 지정한다.
     *     public OnPlayServerUpdateHealth() {
     *         super(DMGR.getPlugin(), PacketType.Play.Server.UPDATE_HEALTH);
     *     }
     *
     *     // 패킷이 송신될 때 호출된다.
     *     // 서버 패킷(PacketType.Play.Server.*)만 구현한다.
     *     @Override
     *     public void onPacketSending(PacketEvent event) {
     *         // 패킷을 전용 Wrapper 클래스에 할당한다.
     *         WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth(event.getPacket());
     *         Player player = event.getPlayer();
     *     }
     *
     *     // 패킷이 수신될 때 호출된다. (PacketType.Play.Client.* 전용)
     *     // 클라이언트 패킷(PacketType.Play.Client.*)만 구현한다.
     *     @Override
     *     public void onPacketReceiving(PacketEvent event) {
     *         // 패킷을 전용 Wrapper 클래스에 할당한다.
     *         WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth(event.getPacket());
     *         Player player = event.getPlayer();
     *     }
     * }
     *
     * }</pre>
     *
     * @param packetAdapter 패킷 이벤트 처리기
     */
    public static void registerPacketListener(@NonNull PacketAdapter packetAdapter) {
        protocolManager.addPacketListener(packetAdapter);
    }
}
