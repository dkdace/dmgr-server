package com.dace.dmgr.game;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
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
     * 일반 매치 종료 후 결과에 따라 플레이어의 MMR을 조정한다.
     *
     * @param gameUser 대상 플레이어
     */
    private void updateMMR(@NonNull GameUser gameUser) {
        UserData userData = gameUser.getUser().getUserData();

        int mmr = userData.getMatchMakingRate();
        int normalPlayCount = userData.getNormalPlayCount();
        double kda = gameUser.getKDARatio();
        double score = gameUser.getScore();
        Timespan playTime = gameUser.getStartTime().until(Timestamp.now());
        int gameAverageMMR = (int) getAverage(UserData::getMatchMakingRate);

        userData.setMatchMakingRate(getFinalMMR(mmr, normalPlayCount, kda, score, playTime, gameAverageMMR));
        userData.addNormalPlayCount();

        ConsoleLogger.info("{0}의 유저 MMR 변동됨: {1} -> {2}, 일반 매치 플레이 횟수: {3}",
                gameUser.getPlayer().getName(),
                mmr, userData.getMatchMakingRate(),
                normalPlayCount + 1);
    }

    /**
     * 랭크 매치 종료 후 결과에 따라 플레이어의 랭크 점수와 MMR을 조정한다.
     *
     * @param gameUser 대상 플레이어
     * @return 랭크 점수 획득량
     */
    private int updateRankRate(@NonNull GameUser gameUser) {
        UserData userData = gameUser.getUser().getUserData();

        int mmr = userData.getMatchMakingRate();
        int rr = userData.getRankRate();
        int rankPlayCount = userData.getRankPlayCount();
        double kda = gameUser.getKDARatio();
        double score = gameUser.getScore();
        Timespan playTime = gameUser.getStartTime().until(Timestamp.now());
        int gameAverageMMR = (int) getAverage(UserData::getMatchMakingRate);
        int gameAverageRR = (int) getAverage(UserData::getRankRate);

        userData.setMatchMakingRate(getFinalMMR(mmr, rankPlayCount, kda, score, playTime, gameAverageMMR));
        userData.addRankPlayCount();

        if (!userData.isRanked()) {
            if (rankPlayCount + 1 >= GeneralConfig.getGameConfig().getRankPlacementPlayCount()) {
                userData.setRankRate(getFinalRankRate(mmr));
                userData.setRanked(true);
            }
        } else {
            userData.setRankRate(getFinalRankRateRanked(mmr, rr, kda, score, playTime, gameAverageRR,
                    winnerTeam == null ? null : winnerTeam == gameUser.getTeam()));
            ConsoleLogger.info("{0}의 RR 변동됨: {1} -> {2}, 랭크 매치 플레이 횟수: {3}",
                    gameUser.getPlayer().getName(),
                    rr,
                    userData.getRankRate(),
                    rankPlayCount + 1);
        }

        return userData.getRankRate() - rr;
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
     * 게임 결과에 따른 최종 MMR을 반환한다.
     *
     * @param mmr        현재 MMR
     * @param playCount  플레이 횟수
     * @param kda        킬/데스
     * @param score      점수
     * @param playTime   플레이 시간
     * @param averageMMR 게임 참여자들의 MMR 평균
     * @return 최종 MMR
     */
    private int getFinalMMR(int mmr, int playCount, double kda, double score, @NonNull Timespan playTime, int averageMMR) {
        int value = (int) Math.min(((getKDARatioCorrection(kda) + getScoreCorrection(score, playTime)) * 10 + averageMMR), 1000);

        double finalMMR;
        if (playCount < GeneralConfig.getGameConfig().getMmrPlayCountThreshold())
            finalMMR = (mmr * playCount / (playCount + 1.0)) + value * (1 / (playCount + 1.0));
        else {
            playCount = GeneralConfig.getGameConfig().getMmrPlayCountThreshold();
            finalMMR = (mmr * (playCount - 1.0) / (playCount)) + value * (1.0 / (playCount));
        }

        return (int) finalMMR;
    }

    /**
     * 게임 결과에 따른 최종 랭크 점수를 반환한다. (배치 미완료)
     *
     * @param mmr 현재 MMR
     * @return 최종 랭크 점수
     */
    private int getFinalRankRate(int mmr) {
        return (int) Math.min(mmr * 0.9, GeneralConfig.getGameConfig().getMaxPlacementRankRate());
    }

    /**
     * 게임 결과에 따른 최종 랭크 점수를 반환한다. (배치 완료)
     *
     * @param mmr       현재 MMR
     * @param rr        현재 랭크 점수
     * @param kda       킬/데스
     * @param score     점수
     * @param playTime  플레이 시간
     * @param averageRR 게임 참여자들의 랭크 점수 평균
     * @param isWin     승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 최종 랭크 점수
     */
    private int getFinalRankRateRanked(int mmr, int rr, double kda, double score, @NonNull Timespan playTime, int averageRR, @Nullable Boolean isWin) {
        return (int) (rr + Math.round(getKDARatioCorrection(kda) + getScoreCorrection(score, playTime) + getWinCorrection(isWin) +
                getPointCorrection(mmr, rr, averageRR)));
    }

    /**
     * 킬/데스 보정치를 반환한다.
     *
     * @param kda 킬/데스
     * @return 킬/데스 보정치
     */
    private double getKDARatioCorrection(double kda) {
        return (kda / GeneralConfig.getGameConfig().getExpectedAverageKDARatio()) * 20;
    }

    /**
     * 게임 점수 보정치를 반환한다.
     *
     * @param score    점수
     * @param playTime 플레이 시간
     * @return 게임 점수 보정치
     */
    private double getScoreCorrection(double score, @NonNull Timespan playTime) {
        return ((score / GeneralConfig.getGameConfig().getExpectedAverageScorePerMinute()) / playTime.toSeconds() / 60) * 20;
    }

    /**
     * 승패 보정치를 반환한다.
     *
     * @param isWin 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 승패 보정치
     */
    private double getWinCorrection(@Nullable Boolean isWin) {
        if (isWin == null)
            return 0;
        return isWin ? 10 : -8;
    }

    /**
     * MMR, 랭크 점수 보정치를 반환한다.
     *
     * @param mmr       MMR
     * @param rr        랭크 점수
     * @param averageRR 게임 참여자들의 랭크 점수 평균
     * @return MMR, 랭크 점수 보정치
     */
    private double getPointCorrection(int mmr, int rr, int averageRR) {
        double averageDiffValue = (GeneralConfig.getGameConfig().getExpectedAverageRankRate() + averageRR) / 2.0 - rr;
        int weightValue = mmr - rr;

        return averageDiffValue * 0.04 + weightValue * 0.1;
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
