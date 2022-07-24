package com.dace.dmgr.user;

import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;

import static com.dace.dmgr.system.EntityList.userList;

public class Lobby {
    public static void spawn(Player player) {
        User user = userList.get(player.getUniqueId());

        player.teleport(player.getWorld().getSpawnLocation());
        user.reset();
    }
}
