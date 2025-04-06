package com.dace.dmgr.command.test;

import com.dace.dmgr.combat.entity.temporary.dummy.Dummy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 훈련용 봇 소환 명령어 클래스.
 *
 * @see Dummy
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DummyCommand implements CommandExecutor {
    @Getter
    private static final DummyCommand instance = new DummyCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        int health = Integer.parseInt(args[0]);

        if (args.length > 1)
            new Dummy(player.getLocation(), health, Boolean.parseBoolean(args[1]));
        else
            new Dummy(player.getLocation(), health, true);

        return true;
    }
}
