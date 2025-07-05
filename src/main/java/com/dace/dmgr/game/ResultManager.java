package com.dace.dmgr.game;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.ToIntFunction;

/**
 * 게임 종료 후 결과 처리 및 보상 지급을 관리하는 클래스.
 */
public final class ResultManager {
    /** 승리 효과음 */
    private static final SoundEffect WIN_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.UI_TOAST_CHALLENGE_COMPLETE).volume(1000).pitch(1.5).build());
    /** 패배 효과음 */
    private static final SoundEffect LOSE_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_BLAZE_DEATH).volume(1000).pitch(0.5).build());
    /** 무승부 효과음 */
    private static final SoundEffect DRAW_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_LEVELUP).volume(1000).pitch(1).build());

    /** 게임 */
    private final Game game;
    /** 승리한 팀. {@code null}이면 무승부 */
    @Nullable
    private final Team winnerTeam;
    /** 랭킹 항목별 플레이어 순위 목록 (랭킹 항목 : (플레이어 : 순위)) */
    private final EnumMap<RankType, HashMap<GameUser, Integer>> rankingMap = new EnumMap<>(RankType.class);

    /**
     * 게임 참여자들의 결과를 처리한다.
     *
     * @param game 대상 게임
     */
    ResultManager(@NonNull Game game) {
        this.game = game;

        Team redTeam = game.getRedTeam();
        Team blueTeam = game.getBlueTeam();

        Team winner = redTeam.getScore() > blueTeam.getScore() ? redTeam : blueTeam;
        if (redTeam.getScore() == blueTeam.getScore())
            winner = null;

        this.winnerTeam = winner;

        for (Team team : new Team[]{redTeam, blueTeam})
            for (RankType rankType : RankType.values()) {
                Iterator<GameUser> iterator = team.getTeamUsers().stream()
                        .sorted(Comparator.comparing(rankType.valueFunction::applyAsInt).reversed())
                        .iterator();
                for (int i = 0; iterator.hasNext(); i++)
                    rankingMap.computeIfAbsent(rankType, k -> new HashMap<>()).put(iterator.next(), i);
            }

        game.getGameUsers().forEach(this::giveReward);
    }

    /**
     * 지정한 플레이어에게 보상을 지급한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void giveReward(@NonNull GameUser gameUser) {
        if (winnerTeam != null) {
            UserData userData = gameUser.getUser().getUserData();

            if (winnerTeam == gameUser.getTeam())
                userData.addWinCount();
            else
                userData.addLoseCount();
        }

        int moneyEarned = updateMoney(gameUser);
        int xpEarned = updateXp(gameUser);
        int rankEarned = 0;

        if (game.getGamePlayMode().isRanked())
            rankEarned = updateRankRate(gameUser);
        else
            updateMMR(gameUser);

        sendResultReport(gameUser, moneyEarned, xpEarned, rankEarned);
    }

    /**
     * 지정한 플레이어에게 전체 결과 메시지를 전송한다.
     *
     * @param gameUser    대상 플레이어
     * @param moneyEarned 획득한 돈
     * @param xpEarned    획득한 경험치
     * @param rankEarned  획득한 랭크 점수
     */
    private void sendResultReport(@NonNull GameUser gameUser, int moneyEarned, int xpEarned, int rankEarned) {
        ChatColor winColor;
        String winText;
        SoundEffect winSound;
        if (winnerTeam == null) {
            winColor = ChatColor.YELLOW;
            winText = "무승부";
            winSound = DRAW_SOUND;
        } else if (winnerTeam == gameUser.getTeam()) {
            winColor = ChatColor.GREEN;
            winText = "승리";
            winSound = WIN_SOUND;
        } else {
            winColor = ChatColor.RED;
            winText = "패배";
            winSound = LOSE_SOUND;
        }

        new DelayTask(() -> {
            gameUser.getUser().sendTitle(winColor + "§l" + winText, "", Timespan.ofSeconds(0.4), Timespan.ofSeconds(2),
                    Timespan.ofSeconds(1.5), Timespan.ofSeconds(2));
            winSound.play(gameUser.getPlayer());
        }, 40);

        StringBuilder result = new StringBuilder(MessageFormat.format(String.join("\n",
                StringFormUtil.BAR,
                "§d§l플레이 정보 {0}§l[{1}]",
                "",
                getPlayInfoReport(gameUser),
                "",
                "§d§l보상 획득",
                "",
                "§e▶ CP 획득 §7:: §6+{2}",
                "§e▶ 경험치 획득 §7:: §6+{3}",
                StringFormUtil.BAR), winColor, winText, moneyEarned, xpEarned));
        if (game.getGamePlayMode().isRanked())
            result.append(MessageFormat.format(String.join("\n",
                    "§d§l랭크",
                    "",
                    "§e▶ 랭크 점수 §7:: {0}",
                    StringFormUtil.BAR), winColor + (rankEarned >= 0 ? "+" : "") + rankEarned));

        gameUser.getUser().clearChat();
        gameUser.getUser().sendMessageInfo(result.toString());
    }

    /**
     * 지정한 플레이어의 플레이 정보 결과 메시지를 반환한다.
     *
     * @param gameUser 대상 플레이어
     * @return 결과 메시지
     */
    @NonNull
    private String getPlayInfoReport(@NonNull GameUser gameUser) {
        ChatColor[] rankColors = {ChatColor.YELLOW, ChatColor.WHITE, ChatColor.GOLD, ChatColor.DARK_GRAY};
        StringJoiner playInfoText = new StringJoiner("\n");

        rankingMap.keySet().forEach(type -> {
            int rankIndex = rankingMap.get(type).get(gameUser);

            String text = MessageFormat.format("{0}§l■ {0}{1} : {2} §l[{3}위]",
                    rankColors[Math.min(rankIndex, 3)],
                    type.name,
                    type.valueFunction.applyAsInt(gameUser),
                    rankIndex + 1);

            playInfoText.add(text);
        });
        playInfoText.add("§8§l■ §8사망 : " + gameUser.getDeath());

        return playInfoText.toString();
    }

    /**
     * 매치 종료 후 결과에 따라 플레이어의 경험치를 증가시킨다.
     *
     * @param gameUser 대상 플레이어
     * @return 획득한 경험치
     */
    private int updateXp(@NonNull GameUser gameUser) {
        UserData userData = gameUser.getUser().getUserData();

        int xp = userData.getXp();
        double score = gameUser.getScore();

        int finalXp = getFinalXp(xp, score, winnerTeam == gameUser.getTeam());
        userData.setXp(finalXp);

        return finalXp - xp;
    }

    /**
     * 매치 종료 후 결과에 따라 플레이어의 돈을 증가시킨다.
     *
     * @param gameUser 대상 플레이어
     * @return 획득한 돈
     */
    private int updateMoney(@NonNull GameUser gameUser) {
        UserData userData = gameUser.getUser().getUserData();

        int money = userData.getMoney();
        double score = gameUser.getScore();

        userData.setMoney(getFinalMoney(money, score, winnerTeam == gameUser.getTeam()));

        return userData.getMoney() - money;
    }


    /**
     * 게임에 참가 중인 모든 유저의 평균 랭크 점수를 구한다.
     *
     * @return 평균 랭크 점수
     */
    private double getAverageScore() {
        return game.getGameUsers().stream().mapToDouble(GameUser::getScore).average().orElse(0);
    }

    /**
     * 게임에 참가 중인 모든 유저의 평균 KDA를 구한다.
     *
     * @return 평균 KDA
     */
    private double getAverageKDA() {
        return game.getGameUsers().stream().mapToDouble(GameUser::getKDARatio).average().orElse(0);
    }

    /**
     * tanh 계산 함수
     *
     * @return tanh
     */
    private static double tanh(double x) {
        return Math.tanh(x);
    }

    /**
     * RR과 MMR의 최저점과 최고점을 넘어서지 않도록 고정한다.
     *
     * @return 0(최저) ~ 1000(최고)
     */
    private static int clamp(int v) {
        return Math.max(0, Math.min(1000, v));
    }

    /**
     * 일반 매치의 MMR을 계산하여 반환한다.
     *
     * @param gameUser 대상 플레이어
     * @return 계산된 MMR (0~1000)
     */
    private int getFinalMMR(GameUser gameUser) {
        UserData userData = gameUser.getUser().getUserData();
        int curMMR        = userData.getMatchMakingRate();
        int avgMMR        = (int) getAverage(UserData::getMatchMakingRate);
        double score      = gameUser.getScore();
        double avgScore   = getAverageScore();
        double kda        = gameUser.getKDARatio();
        double avgKDA     = getAverageKDA();
        boolean isWin     = (winnerTeam != null && winnerTeam == gameUser.getTeam());

        double deltaMMR    = curMMR - avgMMR;
        double deltaScore  = score  - avgScore;
        double deltaKDA100 = (kda - avgKDA) * 100;
        double winCoef     = isWin ? 1.0 : -0.8;

        long termMMR   = Math.round(deltaMMR * 0.25);
        long termScore = Math.round(0.8 * 100 * tanh(0.008 / 3.14 * deltaScore));
        long termKDA   = Math.round(0.2 * 100 * tanh(0.008 / 3.14 * deltaKDA100));
        double termWin = winCoef * 20;

        int sumR       = (int)(termScore + termKDA - termMMR + termWin);
        int rawNewMMR  = curMMR + sumR;
        int finalMMR   = (int)Math.round(curMMR * 0.8 + rawNewMMR * 0.2);

        return clamp(finalMMR);
    }

    /**
     * 랭크 매치의 RR을 계산하여 반환한다.
     *
     * @param gameUser 대상 플레이어
     * @param newMMR   갱신된 MMR 값
     * @return 계산된 RR (0~1000)
     */
    private int getFinalRR(GameUser gameUser, int newMMR) {
        UserData userData = gameUser.getUser().getUserData();
        int curRR         = userData.getRankRate();
        int avgRR         = (int) getAverage(UserData::getRankRate);
        boolean isWin     = (winnerTeam != null && winnerTeam == gameUser.getTeam());
        double winCoef    = isWin ? 1.0 : -0.8;
        double termWin    = winCoef * 20;

        double deltaRR    = curRR - avgRR;
        long expDelta     = Math.round(10 * tanh(deltaRR / 150.0));
        long mmrToRR      = Math.round((newMMR - curRR) * 0.1);
        int deltaRank     = (int)(termWin - expDelta + mmrToRR);
        int finalRR       = curRR + deltaRank;

        return clamp(finalRR);
    }

    /**
     * 매치 종료 후 결과에 따라 플레이어의 MMR을 업데이트한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void updateMMR(@NonNull GameUser gameUser) {
        UserData userData = gameUser.getUser().getUserData();
        int newMMR        = getFinalMMR(gameUser);
        userData.setMatchMakingRate(newMMR);
        userData.addNormalPlayCount();
        ConsoleLogger.info("{} MMR: {} -> {}", gameUser.getPlayer().getName(), userData.getMatchMakingRate(), newMMR);
    }

    /**
     * 매치 종료 후 결과에 따라 플레이어의 RR을 업데이트한다.
     *
     * @param gameUser 대상 플레이어
     * @return 획득한 RR 변화량
     */
    private int updateRankRate(@NonNull GameUser gameUser) {
        UserData userData = gameUser.getUser().getUserData();
        int newMMR        = getFinalMMR(gameUser);
        userData.setMatchMakingRate(newMMR);
        int oldRR         = userData.getRankRate();
        int newRR         = getFinalRR(gameUser, newMMR);
        userData.setRankRate(newRR);
        userData.addRankPlayCount();
        ConsoleLogger.info("{} RR: {} -> {}", gameUser.getPlayer().getName(), oldRR, newRR);
        return newRR - oldRR;
    }



    /**
     * 게임 참여자들의 특정 수치의 평균을 반환한다.
     *
     * @param valueFunction 수치 값 반환에 실행할 작업
     * @return 평균 값
     */
    private double getAverage(@NonNull ToIntFunction<@NonNull UserData> valueFunction) {
        return game.getGameUsers().stream()
                .mapToInt(gameUser -> valueFunction.applyAsInt(gameUser.getUser().getUserData()))
                .average()
                .orElse(0);
    }

    /**
     * 게임 결과에 따른 최종 경험치를 반환한다.
     *
     * @param xp    현재 경험치
     * @param score 점수
     * @param isWin 승리 여부
     * @return 최종 경험치
     */
    private int getFinalXp(int xp, double score, boolean isWin) {
        int finalScore = (int) (xp + 50 + score * 0.2);
        if (isWin)
            finalScore += 200;

        return finalScore;
    }

    /**
     * 게임 결과에 따른 최종 금액을 반환한다.
     *
     * @param money 현재 보유 중인 돈
     * @param score 점수
     * @param isWin 승리 여부
     * @return 최종 금액
     */
    private int getFinalMoney(int money, double score, boolean isWin) {
        int finalScore = (int) (money + 50 + score * 0.2);
        if (isWin)
            finalScore += 200;

        return finalScore;
    }

    /**
     * 게임 랭킹 항목의 종류.
     */
    @AllArgsConstructor
    private enum RankType {
        SCORE("점수", gameUser -> (int) gameUser.getScore()),
        DAMAGE("입힌 피해", gameUser -> (int) gameUser.getDamage()),
        KILL("적 처치", GameUser::getKill),
        DEFEND("막은 피해", gameUser -> (int) gameUser.getDefend()),
        HEAL("치유", gameUser -> (int) gameUser.getHeal());

        /** 항목 이름 */
        private final String name;
        /** 항목 값 반환에 실행할 작업 */
        private final ToIntFunction<GameUser> valueFunction;
    }
}
