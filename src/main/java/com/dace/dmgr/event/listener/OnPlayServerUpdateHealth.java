package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayServerUpdateHealth;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.event.PacketEventListener;
import lombok.Getter;
import lombok.NonNull;

public final class OnPlayServerUpdateHealth extends PacketEventListener<WrapperPlayServerUpdateHealth> {
    @Getter
    private static final OnPlayServerUpdateHealth instance = new OnPlayServerUpdateHealth();

    private OnPlayServerUpdateHealth() {
        super(WrapperPlayServerUpdateHealth.class);
    }

    @Override
    protected void onEvent(@NonNull PacketEvent event) {
        WrapperPlayServerUpdateHealth packet = createPacketWrapper(event);

        if (event.getPlayer().getFoodLevel() == packet.getFood())
            event.setCancelled(true);
    }
}
