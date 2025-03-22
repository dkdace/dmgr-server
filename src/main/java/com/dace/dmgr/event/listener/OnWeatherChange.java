package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.weather.WeatherChangeEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnWeatherChange extends EventListener<WeatherChangeEvent> {
    @Getter
    private static final OnWeatherChange instance = new OnWeatherChange();

    @EventHandler
    protected void onEvent(@NonNull WeatherChangeEvent event) {
        if (event.toWeatherState())
            event.setCancelled(true);
    }
}
