package com.dace.dmgr.system.command.test;

import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
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
 * @see CombatUser#setCharacterType(CharacterType)
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
        String character = args[2].toUpperCase();

        CombatUser combatUser = EntityInfoRegistry.getCombatUser(player);
        if (combatUser == null) {
            GameUser gameUser = EntityInfoRegistry.getGameUser(player);

            if (gameUser == null)
                combatUser = new CombatUser(player);
            else
                combatUser = new CombatUser(player, gameUser);

            combatUser.init();
        }
        combatUser.setTeam(team);
        combatUser.setCharacterType(CharacterType.valueOf(character));

        return true;
    }
}
