package com.dace.dmgr.command.test;

import com.dace.dmgr.combat.entity.temporary.dummy.Dummy;
import com.dace.dmgr.command.CommandHandler;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

/**
 * 훈련용 봇 소환 명령어 클래스.
 *
 * @see Dummy
 */
public final class DummyCommand extends CommandHandler {
    @Getter
    private static final DummyCommand instance = new DummyCommand();

    private DummyCommand() {
        super("소환", new ParameterList(() -> Collections.singletonList("1000"), () -> Arrays.asList("true", "false")));
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        int health = Integer.parseInt(args[0]);

        if (args.length > 1)
            new Dummy(sender.getLocation(), health, 0, Boolean.parseBoolean(args[1]));
        else
            new Dummy(sender.getLocation(), health, 0, true);
    }
}
