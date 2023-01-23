package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayServerNamedSoundEffect;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import org.bukkit.entity.Player;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class OnPlayServerNamedSoundEffect extends PacketAdapter {
    public OnPlayServerNamedSoundEffect() {
        super(DMGR.getPlugin(), PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WrapperPlayServerNamedSoundEffect packet = new WrapperPlayServerNamedSoundEffect(event.getPacket());
        Player player = event.getPlayer();
        CombatUser combatUser = combatUserMap.get(player);

        if (combatUser != null)
            if (CooldownManager.getCooldown(combatUser, Cooldown.SILENCE) > 0)
                event.setCancelled(true);
    }
}
