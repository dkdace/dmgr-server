package com.dace.dmgr.system.command;

import com.dace.dmgr.admin.Admin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * 전투원 선택 명령어 클래스.
 *
 * <p>Usage: /선택 플레이어 팀 전투원</p>
 */
public class SelectCharCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Admin.selectCharacter(Bukkit.getPlayer(args[0]), args[1], args[2]);

        return true;
    }
}
