package com.dace.dmgr.command;

import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.LocationUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 로비 이동 명령어 클래스.
 *
 * <p>Usage: /스폰</p>
 *
 * @see LocationUtil#getLobbyLocation()
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LobbyCommand implements CommandExecutor {
    @Getter
    private static final LobbyCommand instance = new LobbyCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        User user = User.fromPlayer(player);
        GameUser gameUser = GameUser.fromUser(user);
        if (gameUser != null) {
            Game game = gameUser.getGame();
            if (game.getPhase() == Game.Phase.READY || game.getPhase() == Game.Phase.PLAYING) {
                user.sendMessageWarn("게임 진행 중에 나가려면 §l§n'/quit'§r 또는 §l§n'/q'§r를 입력하십시오.");
                return true;
            }
        }

        user.reset();

        return true;
    }
}
