package com.dace.dmgr.user;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static com.dace.dmgr.system.EntityList.userList;

public class Lobby {
    public static Location lobby = new Location(Bukkit.getWorld("DMGR"), 72.5, 64, 39.5, 90, 0);

    public static void spawn(Player player) {
        User user = userList.get(player.getUniqueId());

        player.teleport(lobby);
        user.reset();
    }
}
