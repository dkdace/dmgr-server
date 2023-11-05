package com.dace.dmgr.system.command.test;

import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.arkace.Arkace;
import com.dace.dmgr.combat.character.jager.Jager;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.Team;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 전투원 선택 명령어 클래스.
 *
 * <p>Usage: /선택 플레이어 팀 전투원</p>
 *
 * @see CombatUser#setCharacter(Character)
 */
public class SelectCharCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = Bukkit.getPlayer(args[0]);
        Team team = Team.NONE;
        if (args[1].equalsIgnoreCase("red"))
            team = Team.RED;
        else if (args[1].equalsIgnoreCase("blue"))
            team = Team.BLUE;
        String character = args[2];

        CombatUser combatUser = EntityInfoRegistry.getCombatUser(player);
        if (combatUser == null) {
            combatUser = new CombatUser(player);
            combatUser.init();
        }
        combatUser.setTeam(team);

        switch (character.toLowerCase()) {
            case "아케이스":
            case "arkace":
                combatUser.setCharacter(Arkace.getInstance());
                break;
            case "예거":
            case "jager":
                combatUser.setCharacter(Jager.getInstance());
                break;
        }

        return true;
    }
}
