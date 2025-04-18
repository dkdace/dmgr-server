package com.dace.dmgr.command.test;

import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 전투원 선택 명령어 클래스.
 *
 * @see CombatUser#setCombatantType(CombatantType)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SelectCharCommand implements CommandExecutor {
    @Getter
    private static final SelectCharCommand instance = new SelectCharCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = Bukkit.getPlayer(args[0]);
        String character = args[1].toUpperCase();

        User user = User.fromPlayer(player);
        CombatUser combatUser = CombatUser.fromUser(user);
        CombatantType combatantType = CombatantType.valueOf(character);

        if (combatUser == null)
            new CombatUser(combatantType, user);
        else
            combatUser.setCombatantType(combatantType);

        return true;
    }
}
