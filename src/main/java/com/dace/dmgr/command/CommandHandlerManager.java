package com.dace.dmgr.command;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.command.test.DummyCommand;
import com.dace.dmgr.command.test.GameTestCommand;
import com.dace.dmgr.command.test.SelectCharCommand;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.Validate;

/**
 * 명령어 처리기를 등록하는 클래스.
 */
@UtilityClass
public final class CommandHandlerManager {
    /**
     * 모든 명령어 처리기를 등록한다.
     *
     * <p>플러그인 활성화 시 호출해야 한다.</p>
     */
    public static void register() {
        Validate.notNull(LobbyCommand.getInstance());
        Validate.notNull(MenuCommand.getInstance());
        Validate.notNull(DiscordCommand.getInstance());
        Validate.notNull(MinelistCommand.getInstance());
        Validate.notNull(HelpCommand.getInstance());
        Validate.notNull(StatCommand.getInstance());
        Validate.notNull(DMCommand.getInstance());
        Validate.notNull(BlockCommand.getInstance());
        Validate.notNull(RankingCommand.getInstance());
        Validate.notNull(TeamChatCommand.getInstance());
        Validate.notNull(WarningCommand.getInstance());
        Validate.notNull(BanCommand.getInstance());
        Validate.notNull(AdminChatCommand.getInstance());

        ConsoleLogger.info("명령어 등록 완료");
    }

    /**
     * 모든 테스트용 명령어 처리기를 등록한다.
     */
    public static void registerTestCommands() {
        DMGR.getPlugin().getCommand("선택").setExecutor(SelectCharCommand.getInstance());
        DMGR.getPlugin().getCommand("소환").setExecutor(DummyCommand.getInstance());
        DMGR.getPlugin().getCommand("게임").setExecutor(GameTestCommand.getInstance());
    }
}
