package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayServerNamedSoundEffect;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;

public final class OnPlayServerNamedSoundEffect extends PacketAdapter {
    public OnPlayServerNamedSoundEffect() {
        super(DMGR.getPlugin(), PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WrapperPlayServerNamedSoundEffect packet = new WrapperPlayServerNamedSoundEffect(event.getPacket());
        Player player = event.getPlayer();
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));

        if (combatUser != null && combatUser.getStatusEffectModule().hasStatusEffect(StatusEffectType.SILENCE))
            event.setCancelled(true);
    }
}
