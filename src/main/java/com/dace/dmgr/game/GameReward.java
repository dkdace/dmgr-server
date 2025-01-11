package com.dace.dmgr.game;

import com.dace.dmgr.GeneralConfig;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

/**
 * 게임 결과에 따른 보상을 계산하는 클래스.
 */
@UtilityClass
final class GameReward {
    /**
     * 게임 결과에 따른 최종 경험치를 반환한다.
     *
     * @param xp       현재 경험치
     * @param score    점수
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 최종 경험치
     */
    static int getFinalXp(int xp, double score, @Nullable Boolean isWinner) {
        int finalScore = (int) (xp + 50 + score * 0.2);
        if (Boolean.TRUE.equals(isWinner))
            finalScore += 200;

        return finalScore;
    }

    /**
     * 게임 결과에 따른 최종 금액을 반환한다.
     *
     * @param money    현재 보유 중인 돈
     * @param score    점수
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 최종 금액
     */
    static int getFinalMoney(int money, double score, @Nullable Boolean isWinner) {
        int finalScore = (int) (money + 50 + score * 0.2);
        if (Boolean.TRUE.equals(isWinner))
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
     * @param playTime   플레이 시간 (초)
     * @param averageMMR 게임 참여자들의 MMR 평균
     * @return 최종 MMR
     */
    static int getFinalMMR(int mmr, int playCount, double kda, double score, int playTime, int averageMMR) {
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
    static int getFinalRankRate(int mmr) {
        return (int) Math.min(mmr * 0.9, GeneralConfig.getGameConfig().getMaxPlacementRankRate());
    }

    /**
     * 게임 결과에 따른 최종 랭크 점수를 반환한다. (배치 완료)
     *
     * @param mmr       현재 MMR
     * @param rr        현재 랭크 점수
     * @param kda       킬/데스
     * @param score     점수
     * @param playTime  플레이 시간 (초)
     * @param averageRR 게임 참여자들의 랭크 점수 평균
     * @param isWinner  승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 최종 랭크 점수
     */
    static int getFinalRankRateRanked(int mmr, int rr, double kda, double score, int playTime, int averageRR, @Nullable Boolean isWinner) {
        return (int) (rr + Math.round(getKDARatioCorrection(kda) + getScoreCorrection(score, playTime)
                + getWinCorrection(isWinner) + getPointCorrection(mmr, rr, averageRR)));
    }

    /**
     * 킬/데스 보정치를 반환한다.
     *
     * @param kda 킬/데스
     * @return 킬/데스 보정치
     */
    private static double getKDARatioCorrection(double kda) {
        return (kda / GeneralConfig.getGameConfig().getExpectedAverageKDARatio()) * 20;
    }

    /**
     * 게임 점수 보정치를 반환한다.
     *
     * @param score    점수
     * @param playTime 플레이 시간 (초)
     * @return 게임 점수 보정치
     */
    private static double getScoreCorrection(double score, long playTime) {
        return ((score / GeneralConfig.getGameConfig().getExpectedAverageScorePerMinute()) / playTime / 60) * 20;
    }

    /**
     * 승패 보정치를 반환한다.
     *
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 승패 보정치
     */
    private static double getWinCorrection(@Nullable Boolean isWinner) {
        if (isWinner == null)
            return 0;
        return isWinner ? 10 : -8;
    }

    /**
     * MMR, 랭크 점수 보정치를 반환한다.
     *
     * @param mmr       MMR
     * @param rr        랭크 점수
     * @param averageRR 게임 참여자들의 랭크 점수 평균
     * @return MMR, 랭크 점수 보정치
     */
    private static double getPointCorrection(int mmr, int rr, int averageRR) {
        double averageDiffValue = (GeneralConfig.getGameConfig().getExpectedAverageRankRate() + averageRR) / 2.0 - rr;
        int weightValue = mmr - rr;
        return averageDiffValue * 0.04 + weightValue * 0.1;
    }
}
