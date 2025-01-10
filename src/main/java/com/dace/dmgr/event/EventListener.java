package com.dace.dmgr.event;

import com.dace.dmgr.DMGR;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * 일반 이벤트를 처리하는 클래스.
 *
 * <p>Example:</p>
 *
 * <pre><code>
 * public class OnPlayerJoin extends EventListener<PlayerJoinEvent> {
 *     {@code @Override}
 *     {@code @EventHandler}
 *     protected void onEvent(PlayerJoinEvent event) {
 *         Player player = event.getPlayer();
 *     }
 * }
 * </code></pre>
 *
 * @param <T> {@link Event}를 상속받는 이벤트
 */
public abstract class EventListener<T extends Event> implements Listener {
    /**
     * 이벤트 처리 인스턴스를 생성하고 등록한다.
     */
    protected EventListener() {
        Bukkit.getPluginManager().registerEvents(this, DMGR.getPlugin());
    }

    /**
     * 이벤트가 발생했을 때 실행할 작업.
     *
     * @param event 발생한 이벤트
     * @apiNote 구현 시 {@link EventHandler}를 사용해야 함
     */
    protected abstract void onEvent(@NonNull T event);
}
