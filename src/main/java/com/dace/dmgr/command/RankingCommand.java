package com.dace.dmgr.command;

import com.dace.dmgr.game.RankUtil;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.StringFormUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

/**
 * 랭킹 명령어 클래스.
 *
 * <p>Usage: /랭킹</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RankingCommand extends BaseCommandExecutor {
    /** 도움말 메시지 */
    private static final String MESSAGE_HELP = StringFormUtil.BAR +
            "\n§a§l/(랭킹|rank[ing]) <항목> - §a1위부터 10위까지의 항목별 랭킹을 확인합니다." +
            "\n§f" +
            "\n§e§l[항목 목록]" +
            "\n§f- §l(점수|티어|tier) - §f랭크 점수 (티어) 랭킹" +
            "\n§f- §l(레벨|level) - §f레벨 랭킹" +
            "\n" + StringFormUtil.BAR;
    @Getter
    private static final RankingCommand instance = new RankingCommand();

    @Override
    protected void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(player);

        if (args.length == 1) {
            ChatColor[] rankColors = {ChatColor.YELLOW, ChatColor.WHITE, ChatColor.GOLD};

            switch (args[0].toLowerCase()) {
                case "점수":
                case "wjatn":
                case "티어":
                case "xldj":
                case "tier": {
                    UserData[] ranking = RankUtil.getRanking(RankUtil.Indicator.RANK_RATE, 10);

                    user.sendMessageInfo(StringFormUtil.BAR);
                    for (int i = 0; i < ranking.length; i++) {
                        user.sendMessageInfo(MessageFormat.format("{0}§l[ {1} ] {2} §f- §b{3}점", i < 3 ? rankColors[i] : "§7§l",
                                String.format("%02d", i + 1), ranking[i].getDisplayName(), ranking[i].getRankRate()));
                    }

                    break;
                }
                case "레벨":
                case "fpqpf":
                case "level": {
                    UserData[] ranking = RankUtil.getRanking(RankUtil.Indicator.LEVEL, 10);

                    user.sendMessageInfo(StringFormUtil.BAR);
                    for (int i = 0; i < ranking.length; i++) {
                        user.sendMessageInfo(MessageFormat.format("{0}§l[ {1} ] {2}", i < 3 ? rankColors[i] : "§7§l",
                                String.format("%02d", i + 1), ranking[i].getDisplayName()));
                    }

                    break;
                }
                default: {
                    user.sendMessageInfo(MESSAGE_HELP);
                    return;
                }
            }

            user.sendMessageInfo("");
            user.sendMessageInfo("§c* 실제 기록이 반영되기까지 다소 시간이 걸릴 수 있습니다.");
            user.sendMessageInfo(StringFormUtil.BAR);
        } else
            user.sendMessageInfo(MESSAGE_HELP);
    }

    @Override
    @Nullable
    protected List<@NonNull String> getCompletions(@NonNull String alias, @NonNull String @NonNull [] args) {
        if (args.length != 1)
            return null;

        switch (alias.toLowerCase()) {
            case "랭킹":
            case "fodzld":
                return Arrays.asList("점수", "티어", "레벨");
            case "rank":
            case "ranking":
                return Arrays.asList("tier", "level");
            default:
                return null;
        }
    }
}
