package com.dace.dmgr.event;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.dace.dmgr.DMGR;
import org.bukkit.event.Listener;

public abstract class EventManager {
    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    protected static void registerListener(Listener listener) {
        DMGR.getPlugin().getServer().getPluginManager().registerEvents(listener, DMGR.getPlugin());
    }

    protected static void registerPacketListener(PacketAdapter packetAdapter) {
        protocolManager.addPacketListener(packetAdapter);
    }
}
