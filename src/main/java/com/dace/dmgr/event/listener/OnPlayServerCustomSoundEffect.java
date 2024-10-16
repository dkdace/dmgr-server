package com.dace.dmgr.event.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.CombatRestrictions;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;

public final class OnPlayServerCustomSoundEffect extends PacketAdapter {
    public OnPlayServerCustomSoundEffect() {
        super(DMGR.getPlugin(), PacketType.Play.Server.CUSTOM_SOUND_EFFECT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();

        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
        if (combatUser == null)
            return;

        if (combatUser.getStatusEffectModule().hasAnyRestriction(CombatRestrictions.HEAR))
            event.setCancelled(true);
    }
}
