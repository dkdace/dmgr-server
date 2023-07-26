package com.dace.dmgr.event.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class OnPlayClientArmAnimation extends PacketAdapter {
    public OnPlayClientArmAnimation() {
        super(DMGR.getPlugin(), PacketType.Play.Client.ARM_ANIMATION);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        CombatUser combatUser = EntityInfoRegistry.getCombatUser(event.getPlayer());

        if (combatUser != null) {
            event.setCancelled(true);

            if (combatUser.getCharacter() != null) {
                CombatUserActionEvent newEvent = new CombatUserActionEvent(combatUser, ActionKey.LEFT_CLICK);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getServer().getPluginManager().callEvent(newEvent);
                    }
                }.runTaskLater(DMGR.getPlugin(), 0);
            }
        }
    }
}
