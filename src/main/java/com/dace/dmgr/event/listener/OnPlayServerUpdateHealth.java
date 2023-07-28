package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayServerUpdateHealth;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.DMGR;
import org.bukkit.entity.Player;

public final class OnPlayServerUpdateHealth extends PacketAdapter {
    public OnPlayServerUpdateHealth() {
        super(DMGR.getPlugin(), PacketType.Play.Server.UPDATE_HEALTH);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth(event.getPacket());
        Player player = event.getPlayer();

        if (player.getFoodLevel() == packet.getFood())
            event.setCancelled(true);
    }
}
