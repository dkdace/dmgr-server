package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.event.PacketEventListener;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

public final class OnPlayServerPlayerInfo extends PacketEventListener<WrapperPlayServerPlayerInfo> {
    @Getter
    private static final OnPlayServerPlayerInfo instance = new OnPlayServerPlayerInfo();

    private OnPlayServerPlayerInfo() {
        super(WrapperPlayServerPlayerInfo.class);
    }

    @Override
    protected void onEvent(@NonNull PacketEvent event) {
        WrapperPlayServerPlayerInfo packet = createPacketWrapper(event);
        Player player = event.getPlayer();

        packet.getData().forEach(playerInfoData -> {
            if (playerInfoData.getProfile().getUUID() == player.getUniqueId())
                User.fromPlayer(player).setPing(playerInfoData.getLatency());
        });
    }
}
