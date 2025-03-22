package com.dace.dmgr.command;

import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 귓속말 명령어 클래스.
 *
 * @see User#setMessageTarget(User)
 */
public final class DMCommand extends CommandHandler {
    @Getter
    private static final DMCommand instance = new DMCommand();

    private DMCommand() {
        super("귓속말", new ParameterList(ParameterType.PLAYER_NAME));
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(sender);

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendWarnPlayerNotFound(sender);
                return;
            }

            user.setMessageTarget(User.fromPlayer(target));
            user.sendMessageInfo("\n" +
                            "§e§n{0}§r님과의 대화가 시작되었습니다.\n" +
                            "종료하려면 §n''{1}''§r을 다시 입력하십시오.\n",
                    target.getName(), getUsage().split(" ")[0]);
        } else {
            if (user.getMessageTarget() == null) {
                sendWarnWrongUsage(sender);
                return;
            }

            user.setMessageTarget(null);
            user.sendMessageInfo("\n개인 대화가 종료되었습니다.\n");
        }
    }
}


