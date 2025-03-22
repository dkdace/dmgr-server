package com.dace.dmgr.command.test;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.game.GameRoom;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * 게임 테스트 명령어 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GameTestCommand implements CommandExecutor {
    @Getter
    private static final GameTestCommand instance = new GameTestCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int number = Integer.parseInt(args[1]);

        switch (args[0]) {
            case "생성": {
                new GameRoom(false, number);
                ConsoleLogger.info("일반 게임 생성됨 : [{0}]", number);

                break;
            }
            case "전체추가": {
                GameRoom gameRoom = GameRoom.fromNumber(false, number);
                User.getAllUsers().forEach(user -> user.joinGame(gameRoom));

                break;
            }
            case "삭제": {
                GameRoom gameRoom = GameRoom.fromNumber(false, number);
                gameRoom.remove();
                ConsoleLogger.info("일반 게임 제거됨 : [{0}]", number);

                break;
            }
        }

        return true;
    }
}
