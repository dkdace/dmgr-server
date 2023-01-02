package com.dace.dmgr.system.command;

import com.dace.dmgr.lobby.Lobby;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 로비 이동 명령어 클래스.
 *
 * <p>Usage: /스폰</p>
 */
public class LobbyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        Lobby.spawn(player);

        return true;
    }
}
