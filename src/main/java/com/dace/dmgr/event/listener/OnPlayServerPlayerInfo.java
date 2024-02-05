package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;

public final class OnPlayServerPlayerInfo extends PacketAdapter {
    public OnPlayServerPlayerInfo() {
        super(DMGR.getPlugin(), PacketType.Play.Server.PLAYER_INFO);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(event.getPacket());
        Player player = event.getPlayer();
        User user = User.fromPlayer(player);

        packet.getData().forEach(playerInfoData -> {
            if (playerInfoData.getProfile().getUUID() == player.getUniqueId())
                user.setPing(playerInfoData.getLatency());
        });
    }
}
