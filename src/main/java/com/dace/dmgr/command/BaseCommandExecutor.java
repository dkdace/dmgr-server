package com.dace.dmgr.command;

import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 명령어의 응답과 인수 자동완성을 처리하는 클래스.
 */
public abstract class BaseCommandExecutor implements TabExecutor {
    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;

        onCommandInput((Player) sender, args);

        return true;
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = getCompletions(alias, args);
        if (args.length == 0 || completions == null || completions.isEmpty())
            return Collections.emptyList();

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * 명령어 입력 시 실행할 작업.
     *
     * @param player 입력자
     * @param args   인수 목록
     */
    protected abstract void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args);

    /**
     * 명령어 인수 자동완성 목록을 반환한다.
     *
     * @param alias 입력한 명령어
     * @param args  인수 목록
     * @return 자동완성 목록. {@code null} 반환 시 자동완성 없음
     */
    @Nullable
    protected abstract List<@NonNull String> getCompletions(@NonNull String alias, @NonNull String @NonNull [] args);
}
