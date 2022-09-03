package com.dace.dmgr.system;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.DMGR;
import org.bukkit.entity.Player;

public class PacketListener {
    public static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public static void init() {
        PacketAdapter playServerUpdateHealth = new PacketAdapter(DMGR.getPlugin(), PacketType.Play.Server.UPDATE_HEALTH) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();

                if (player.getFoodLevel() == event.getPacket().getIntegers().readSafely(0))
                    event.setCancelled(true);
            }
        };

        protocolManager.addPacketListener(playServerUpdateHealth);
    }
}
