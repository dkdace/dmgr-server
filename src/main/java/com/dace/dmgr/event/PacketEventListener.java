package com.dace.dmgr.event;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.DMGR;
import lombok.NonNull;

import java.lang.reflect.Constructor;

/**
 * 패킷 이벤트를 처리하는 클래스.
 *
 * <p>Example:</p>
 *
 * <pre><code>
 * public class OnPlayServerUpdateHealth extends PacketEventListener<WrapperPlayServerUpdateHealth> {
 *     {@code @Override}
 *     protected void onEvent(PacketEvent event) {
 *         // 패킷 Wrapper를 사용하여 패킷의 내용에 접근할 수 있다.
 *         WrapperPlayServerUpdateHealth packet = createPacketWrapper(event);
 *         Player player = event.getPlayer();
 *     }
 * }
 * </code></pre>
 *
 * @param <T> {@link AbstractPacket}를 상속받는 패킷 Wrapper
 */
public abstract class PacketEventListener<T extends AbstractPacket> extends PacketAdapter {
    /** 패킷 Wrapper 클래스 인스턴스 */
    private final Class<T> packetWrapperClass;
    /** 패킷 Wrapper의 생성자 인스턴스 */
    private Constructor<T> packetWrapperConstructor;

    /**
     * 패킷 이벤트 처리 인스턴스를 생성하고 등록한다.
     *
     * @throws UnsupportedOperationException 해당 패킷 이벤트 처리기를 생성할 수 없으면 발생
     */
    protected PacketEventListener(@NonNull Class<T> packetWrapperClass) {
        super(DMGR.getPlugin(), getPacketType(packetWrapperClass));

        this.packetWrapperClass = packetWrapperClass;
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    /**
     * 지정한 클래스에서 패킷 타입을 반환한다.
     *
     * @param packetWrapperClass 패킷 Wrapper 클래스
     * @return 패킷 타입
     */
    @NonNull
    private static PacketType getPacketType(@NonNull Class<? extends AbstractPacket> packetWrapperClass) {
        try {
            return (PacketType) packetWrapperClass.getDeclaredField("TYPE").get(null);
        } catch (NoSuchFieldException ex) {
            throw new UnsupportedOperationException("해당 패킷 이벤트 처리기를 생성할 수 없음");
        } catch (Exception ex) {
            ConsoleLogger.severe("패킷 이벤트 처리 인스턴스 생성 실패", ex);
        }

        throw new UnsupportedOperationException();
    }

    /**
     * 패킷 이벤트로부터 패킷 Wrapper 인스턴스를 생성한다.
     *
     * @param event 발생한 패킷 이벤트
     * @return 패킷 Wrapper 인스턴스
     * @throws UnsupportedOperationException 해당 패킷 Wrapper 를 생성할 수 없으면 발생
     */
    @NonNull
    protected T createPacketWrapper(@NonNull PacketEvent event) {
        try {
            if (packetWrapperConstructor == null)
                packetWrapperConstructor = packetWrapperClass.getDeclaredConstructor(PacketContainer.class);

            return packetWrapperConstructor.newInstance(event.getPacket());
        } catch (NoSuchMethodException ex) {
            throw new UnsupportedOperationException("해당 PacketWrapper를 생성할 수 없음");
        } catch (Exception ex) {
            ConsoleLogger.severe("PacketWrapper 인스턴스 생성 실패", ex);
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final void onPacketSending(PacketEvent event) {
        onEvent(event);
    }

    @Override
    public final void onPacketReceiving(PacketEvent event) {
        onEvent(event);
    }

    /**
     * 패킷이 송신 또는 수신되었을 때 실행할 작업.
     *
     * <p>{@link PacketEventListener#createPacketWrapper(PacketEvent)}를 이용하여 패킷 Wrapper 인스턴스를 생성하고 패킷의 내용에 접근할 수 있다.</p>
     *
     * @param event 발생한 패킷 이벤트
     * @see PacketEventListener#createPacketWrapper(PacketEvent)
     */
    protected abstract void onEvent(@NonNull PacketEvent event);
}
