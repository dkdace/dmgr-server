package com.dace.dmgr.event;

import com.dace.dmgr.DMGR;
import org.bukkit.event.Listener;

public abstract class EventManager {
    protected static void registerListener(Listener listener) {
        DMGR.getPlugin().getServer().getPluginManager().registerEvents(listener, DMGR.getPlugin());
    }
}
