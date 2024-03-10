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

        WrappedWatchableObject metadata = packet.getMetadata().get(0);

        if (metadata == null || metadata.getIndex() != 0 || entity == null)
            return;

        if (entity instanceof ArmorStand && ((ArmorStand) entity).isSmall())
            return;

        WrappedDataWatcher dw = WrappedDataWatcher.getEntityWatcher(entity);
        dw.deepClone();

        if (GlowUtil.isGlowing(entity, player))
            metadata.setValue((byte) ((byte) metadata.getValue() | (1 << 6)));
        else
            metadata.setValue((byte) ((byte) metadata.getValue() & ~(1 << 6)));
    }
}
