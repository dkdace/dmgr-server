package com.dace.dmgr.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 명령어 목록 확인 명령어 클래스.
 *
 * <p>Usage: /명령어</p>
 */
public class HelpCommand implements CommandExecutor {
    /** 명령어 목록 표시 메시지 */
    public static final String MESSAGE_HELP = "§7==============================" +
            "\n§a§l/(메뉴|menu) - §a메뉴 창을 엽니다. §nF키§a를 눌러 사용할 수도 있습니다." +
            "\n§a§l/(스폰|spawn|exit) - §a스폰(로비)으로 이동합니다." +
            "\n§a§l/(퇴장|quit) - §a현재 입장한 게임에서 나갑니다." +
            "\n§a§l/(전적|stat) - §a개인 전적을 확인합니다." +
            "\n§7==============================";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        player.sendMessage(MESSAGE_HELP);

        return true;
    }
}
