package com.dace.dmgr.system.command.test;

import com.dace.dmgr.lobby.User;
import com.kiwi.dmgr.game.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.dace.dmgr.system.HashMapList.userMap;
import static com.kiwi.dmgr.game.GameMapList.gameUserMap;

public class StatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = Bukkit.getServer().getPlayer(args[0]);
        if (player.isOnline()) {
            User user = userMap.get(player);
            GameUser gameUser = gameUserMap.get(player);
            if (args[1].equals("get")) {
                if (args[2].equals("mmr"))
                    sender.sendMessage(String.valueOf(user.getMMR()));
                if (args[2].equals("rank"))
                    sender.sendMessage(String.valueOf(user.getRank()));
                if (args[2].equals("kill"))
                    sender.sendMessage(String.valueOf(gameUser.getKill()));
                if (args[2].equals("death"))
                    sender.sendMessage(String.valueOf(gameUser.getDeath()));
                if (args[2].equals("score"))
                    sender.sendMessage(String.valueOf(gameUser.getScore()));
                }
            else if (args[1].equals("set")) {
                if (args[2].equals("mmr"))
                    user.setMMR(Integer.parseInt(args[3]));
                if (args[2].equals("rank"))
                    user.setRank(Integer.parseInt(args[3]));
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