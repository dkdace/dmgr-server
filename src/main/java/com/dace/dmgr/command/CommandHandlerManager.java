package com.dace.dmgr.command;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.command.test.DummyCommand;
import com.dace.dmgr.command.test.SelectCharCommand;
import com.dace.dmgr.util.ReflectionUtil;
import lombok.experimental.UtilityClass;

/**
 * 명령어 처리기를 등록하는 클래스.
 */
@UtilityClass
public final class CommandHandlerManager {
    static {
        ReflectionUtil.loadClass(LobbyCommand.class);
        ReflectionUtil.loadClass(MenuCommand.class);
        ReflectionUtil.loadClass(DiscordCommand.class);
        ReflectionUtil.loadClass(MinelistCommand.class);
        ReflectionUtil.loadClass(HelpCommand.class);
        ReflectionUtil.loadClass(StatCommand.class);
        ReflectionUtil.loadClass(DMCommand.class);
        ReflectionUtil.loadClass(BlockCommand.class);
        ReflectionUtil.loadClass(RankingCommand.class);
        ReflectionUtil.loadClass(TeamChatCommand.class);
        ReflectionUtil.loadClass(WarningCommand.class);
        ReflectionUtil.loadClass(BanCommand.class);
        ReflectionUtil.loadClass(AdminChatCommand.class);

        ReflectionUtil.loadClass(SelectCharCommand.class);
        ReflectionUtil.loadClass(DummyCommand.class);

        ConsoleLogger.info("명령어 등록 완료");
    }
}
