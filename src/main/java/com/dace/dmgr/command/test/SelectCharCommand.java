package com.dace.dmgr.command.test;

import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.command.CommandHandler;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 전투원 선택 명령어 클래스.
 *
 * @see CombatUser#setCombatantType(CombatantType)
 */
public final class SelectCharCommand extends CommandHandler {
    @Getter
    private static final SelectCharCommand instance = new SelectCharCommand();

    private SelectCharCommand() {
        super("선택", new ParameterList(
                ParameterType.PLAYER_NAME,
                () -> Arrays.stream(CombatantType.values()).map(Enum::name).collect(Collectors.toList())));
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        Player player = Bukkit.getPlayer(args[0]);
        User user = User.fromPlayer(player);
        CombatUser combatUser = CombatUser.fromUser(user);

        CombatantType combatantType = CombatantType.valueOf(args[1].toUpperCase());

        if (combatUser == null)
            new CombatUser(combatantType, user);
        else
            combatUser.setCombatantType(combatantType);
    }
}
