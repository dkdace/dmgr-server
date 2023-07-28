package com.dace.dmgr.event.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public final class OnWeatherChange implements Listener {
    @EventHandler
    public static void event(WeatherChangeEvent event) {
        if (event.toWeatherState()) event.setCancelled(true);
    }
}
