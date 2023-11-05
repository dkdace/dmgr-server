package com.dace.dmgr.system.command.test;

import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.lobby.User;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = Bukkit.getServer().getPlayer(args[0]);
        if (player.isOnline()) {
            User user = EntityInfoRegistry.getUser(player);
            GameUser gameUser = EntityInfoRegistry.getGameUser(player);

            if (args[1].equals("get")) {
                if (args[2].equals("mmr"))
                    sender.sendMessage(String.valueOf(user.getMatchMakingRate()));
                if (args[2].equals("rank"))
                    sender.sendMessage(String.valueOf(user.getRankRate()));
                if (args[2].equals("kill"))
                    sender.sendMessage(String.valueOf(gameUser.getKill()));
                if (args[2].equals("death"))
                    sender.sendMessage(String.valueOf(gameUser.getDeath()));
                if (args[2].equals("score"))
                    sender.sendMessage(String.valueOf(gameUser.getScore()));
            } else if (args[1].equals("set")) {
                if (args[2].equals("mmr"))
                    user.setMatchMakingRate(Integer.parseInt(args[3]));
                if (args[2].equals("rank"))
                    user.setRankRate(Integer.parseInt(args[3]));
                if (args[2].equals("kill"))
                    gameUser.setKill(Integer.parseInt(args[3]));
                if (args[2].equals("death"))
                    gameUser.setDeath(Integer.parseInt(args[3]));
                if (args[2].equals("score"))
                    gameUser.setScore(Integer.parseInt(args[3]));
            }
        }

        return true;
    }

}
