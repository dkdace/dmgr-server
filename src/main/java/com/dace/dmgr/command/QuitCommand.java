package com.dace.dmgr.command;

import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 게임 퇴장 명령어 클래스.
 *
 * <p>Usage: /퇴장</p>
 *
 * @see GameUser#dispose()
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuitCommand implements CommandExecutor {
    @Getter
    private static final QuitCommand instance = new QuitCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        User user = User.fromPlayer(player);
        GameUser gameUser = GameUser.fromUser(user);

        if (gameUser != null) {
            Game game = gameUser.getGame();
            if (game.getPhase() == Game.Phase.READY || game.getPhase() == Game.Phase.PLAYING)
                user.reset();

            gameUser.dispose();
        }

        return true;
    }
}
