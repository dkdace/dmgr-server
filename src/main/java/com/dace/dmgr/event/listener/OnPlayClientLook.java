package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayClientLook;
import com.comphenix.packetwrapper.WrapperPlayServerPosition;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;

public final class OnPlayClientLook extends PacketAdapter {
    public OnPlayClientLook() {
        super(DMGR.getPlugin(), PacketType.Play.Client.LOOK);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        WrapperPlayClientLook packet = new WrapperPlayClientLook(event.getPacket());
        Player player = event.getPlayer();
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));

        if (combatUser == null || !(combatUser.getWeapon() instanceof Aimable))
            return;
        if (!((Aimable) combatUser.getWeapon()).getAimModule().isAiming())
            return;

        WrapperPlayServerPosition newPacket = new WrapperPlayServerPosition();

        newPacket.setX(0);
        newPacket.setY(0);
        newPacket.setZ(0);
        newPacket.setYaw(((packet.getYaw() % 360) - player.getLocation().getYaw()) * -0.15F);
        newPacket.setPitch((packet.getPitch() - player.getLocation().getPitch()) * -0.15F);
        newPacket.setFlags(new HashSet<>(Arrays.asList(WrapperPlayServerPosition.PlayerTeleportFlag.values())));

        newPacket.sendPacket(player);
    }
}
