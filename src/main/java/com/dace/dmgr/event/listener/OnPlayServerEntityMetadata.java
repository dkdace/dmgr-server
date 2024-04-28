package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.util.GlowUtil;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class OnPlayServerEntityMetadata extends PacketAdapter {
    public OnPlayServerEntityMetadata() {
        super(DMGR.getPlugin(), PacketType.Play.Server.ENTITY_METADATA);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event.getPacket());
        Player player = event.getPlayer();
        Entity entity = packet.getEntity(event);

        for (WrappedWatchableObject wo : packet.getMetadata()) {
            if (wo.getIndex() != 0 || entity == null)
                continue;
            if (entity instanceof ArmorStand && ((ArmorStand) entity).isSmall())
                continue;

            WrappedDataWatcher dw = WrappedDataWatcher.getEntityWatcher(entity);
            dw.deepClone();

            if (GlowUtil.isGlowing(entity, player))
                wo.setValue((byte) ((byte) wo.getValue() | (1 << 6)));
            else
                wo.setValue((byte) ((byte) wo.getValue() & ~(1 << 6)));
        }
    }
}
