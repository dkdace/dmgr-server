package com.dace.dmgr.command;

import com.dace.dmgr.game.RankUtil;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.function.Function;

/**
 * 랭킹 명령어 클래스.
 *
 * @see RankUtil
 */
public final class RankingCommand extends CommandHandler {
    @Getter
    private static final RankingCommand instance = new RankingCommand();

    /** 랭킹 데이터 수 */
    private static final int RANK_LIMIT = 10;
    /** 도움말 메시지 */
    private static final String MESSAGE_HELP = String.join("\n",
            StringFormUtil.BAR,
            MessageFormat.format("§a1위부터 {0}위까지의 항목별 랭킹을 확인합니다.", RANK_LIMIT),
            "",
            instance.score.getHelp(),
            instance.level.getHelp(),
            StringFormUtil.BAR);

    private final Score score = new Score();
    private final Level level = new Level();

    private RankingCommand() {
        super("랭킹");
    }

    /**
     * 랭킹 결과를 전송한다.
     *
     * @param sender        입력자
     * @param indicator     랭킹 데이터 지표
     * @param valueFunction 항목 값 반환에 실행할 작업
     */
    private static void sendResult(@NonNull Player sender, @NonNull RankUtil.Indicator indicator,
                                   @NonNull Function<@NonNull UserData, @NonNull String> valueFunction) {
        User user = User.fromPlayer(sender);
        UserData[] ranking = RankUtil.getRanking(indicator, RANK_LIMIT);
        ChatColor[] rankColors = {ChatColor.YELLOW, ChatColor.WHITE, ChatColor.GOLD};

        user.sendMessageInfo(StringFormUtil.BAR);
        for (int i = 0; i < ranking.length; i++)
            user.sendMessageInfo(MessageFormat.format("{0}§l[ {1} ] {2} {3}", i < 3 ? rankColors[i] : "§7§l",
                    String.format("%02d", i + 1), ranking[i].getDisplayName(), valueFunction.apply(ranking[i])));

        user.sendMessageInfo("\n" +
                "§c* 실제 기록이 반영되기까지 다소 시간이 걸릴 수 있습니다.\n" +
                StringFormUtil.BAR);
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        User.fromPlayer(sender).sendMessageInfo(MESSAGE_HELP);
    }

    /**
     * 티어 랭킹 명령어 클래스.
     */
    private final class Score extends Subcommand {
        private Score() {
            super("(점수|티어|tier)", "랭크 점수 (티어) 랭킹", "점수", "티어", "tier");
        }

        @Override
        protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
            sendResult(sender, RankUtil.Indicator.RANK_RATE, userData ->
                    MessageFormat.format("§f- §b{0}점", userData.getRankRate()));
        }
    }

    /**
     * 레벨 랭킹 명령어 클래스.
     */
    private final class Level extends Subcommand {
        private Level() {
            super("(레벨|level)", "레벨 랭킹", "레벨", "level");
        }

        @Override
        protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
            sendResult(sender, RankUtil.Indicator.LEVEL, userData -> "");
        }
    }
}
