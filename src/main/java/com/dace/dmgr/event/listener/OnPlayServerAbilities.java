package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayServerAbilities;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;

public final class OnPlayServerAbilities extends PacketAdapter {
    public OnPlayServerAbilities() {
        super(DMGR.getPlugin(), PacketType.Play.Server.ABILITIES);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WrapperPlayServerAbilities packet = new WrapperPlayServerAbilities(event.getPacket());
        Player player = event.getPlayer();
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));

        if (combatUser == null)
            return;

        packet.setWalkingSpeed((float) (combatUser.getMoveModule().getSpeedStatus().getValue() * 2 * combatUser.getFovValue()));
    }
}
