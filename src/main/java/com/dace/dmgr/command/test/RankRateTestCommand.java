package com.dace.dmgr.command.test;

import com.dace.dmgr.user.UserData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 랭크 점수 설정 명령어 클래스.
 */
public class RankRateTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        int rr = Integer.parseInt(args[0]);

        UserData.fromPlayer(player).setRankRate(rr);

        return true;
    }
}
