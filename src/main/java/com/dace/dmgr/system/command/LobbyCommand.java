package com.dace.dmgr.system.command;

import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.lobby.Lobby;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 로비 이동 명령어 클래스.
 *
 * <p>Usage: /스폰</p>
 *
 * @see Lobby#spawn(Player)
 */
public class LobbyCommand implements CommandExecutor {
    /** 게임 중 로비 명령어 사용 시 표시되는 메시지 */
    private static final String CANNOT_QUIT_MESSAGE = "§c게임 진행 중에 나가려면 &c&l&n'/quit'&c 또는 &c&l&n'/q'&c를 입력하십시오.";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        GameUser gameUser = EntityInfoRegistry.getGameUser(player);
        if (gameUser != null) {
            Game game = gameUser.getGame();
            if (game.getPhase() == Game.Phase.READY || game.getPhase() == Game.Phase.PLAYING) {
                MessageUtil.sendMessageWarn(player, CANNOT_QUIT_MESSAGE);
                return true;
            }
        }

        Lobby.spawn(player);

        return true;
    }
}
