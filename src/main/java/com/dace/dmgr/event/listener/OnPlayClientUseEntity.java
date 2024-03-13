package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayClientUseEntity;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class OnPlayClientUseEntity extends PacketAdapter {
    public OnPlayClientUseEntity() {
        super(DMGR.getPlugin(), PacketType.Play.Client.USE_ENTITY);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        WrapperPlayClientUseEntity packet = new WrapperPlayClientUseEntity(event.getPacket());
        Player player = event.getPlayer();
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));

        if (combatUser == null)
            return;

        if (packet.getType() == EnumWrappers.EntityUseAction.ATTACK)
            new BukkitRunnable() {
                @Override
                public void run() {
                    combatUser.useAction(ActionKey.LEFT_CLICK);
                }
            }.runTask(DMGR.getPlugin());
    }
}
