package com.dace.dmgr.system.command;

import com.dace.dmgr.gui.menu.PlayerOption;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 메뉴 - 설정 명령어 클래스.
 *
 * <p>Usage: /설정</p>
 *
 * @see PlayerOption
 */
public class PlayerOptionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        PlayerOption playerOption = PlayerOption.getInstance();
        playerOption.open(player);

        return true;
    }
}
