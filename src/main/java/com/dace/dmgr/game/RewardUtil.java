package com.dace.dmgr.game;

/**
 * 게임 보상 관련 기능을 제공하는 클래스.
 */
public final class RewardUtil {
    /** 예상하는 플레이어의 평균 랭크 점수 */
    private static final int EXPECTED_AVERAGE_RANK = 400;
    /** 예상하는 K/DA 평균 */
    private static final float EXPECTED_AVERAGE_KDA = 2F;
    /** 예상하는 분당 획득 점수의 평균 */
    private static final float EXPECTED_AVERAGE_SCORE_PER_MIN = 100F;
    /** 배치로 얻을 수 있는 최대 랭크 점수 */
    private static final int MAX_PLACEMENT_RANK = Tier.EMERALD.getMinScore() - 1;
    /** MMR 수치에 영향을 미치는 플레이 횟수 */
    private static final int MMR_PLAY_COUNT_THRESHOLD = 25;

    /**
     * 게임 결과에 따른 최종 경험치를 반환한다.
     *
     * @param xp       현재 경험치
     * @param score    점수
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 최종 경험치
     */
    public static int getFinalXp(int xp, double score, Boolean isWinner) {
        int finalScore = (int) (xp + 50 + score * 0.2);
        if (isWinner != null && isWinner)
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
    public static int getFinalMoney(int money, double score, Boolean isWinner) {
        int finalScore = (int) (money + 50 + score * 0.2);
        if (isWinner != null && isWinner)
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
    public static int getFinalMMR(int mmr, int playCount, float kda, double score, int playTime, int averageMMR) {
        int value = (int) Math.min(((getKDARatioCorrection(kda) + getScoreCorrection(score, playTime)) * 10 + averageMMR), 1000);

        double finalMMR;
        if (playCount < MMR_PLAY_COUNT_THRESHOLD) {
            finalMMR = (mmr * playCount / (playCount + 1F)) + value * (1F / (playCount + 1));
        } else {
            playCount = MMR_PLAY_COUNT_THRESHOLD;
            finalMMR = (mmr * (playCount - 1F) / (playCount)) + value * (1F / (playCount));
        }

        return (int) finalMMR;
    }

    /**
     * 게임 결과에 따른 최종 랭크 점수를 반환한다. (배치 미완료)
     *
     * @param mmr 현재 MMR
     * @return 최종 랭크 점수
     */
    public static int getFinalRankRate(int mmr) {
        return (int) Math.min(mmr * 0.9, MAX_PLACEMENT_RANK);
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
    public static int getFinalRankRateRanked(int mmr, int rr, float kda, double score, int playTime, int averageRR, Boolean isWinner) {
        return (int) (rr + Math.round(getKDARatioCorrection(kda) + getScoreCorrection(score, playTime) +
                getWinCorrection(isWinner) + getPointCorrection(mmr, rr, averageRR)));
    }

    /**
     * 킬/데스 보정치를 반환한다.
     *
     * @param kda 킬/데스
     * @return 킬/데스 보정치
     */
    private static double getKDARatioCorrection(float kda) {
        return (kda / EXPECTED_AVERAGE_KDA) * 20;
    }

    /**
     * 게임 점수 보정치를 반환한다.
     *
     * @param score    점수
     * @param playTime 플레이 시간 (초)
     * @return 게임 점수 보정치
     */
    private static double getScoreCorrection(double score, long playTime) {
        return ((score / EXPECTED_AVERAGE_SCORE_PER_MIN) / playTime / 60) * 20;
    }

    /**
     * 승패 보정치를 반환한다.
     *
     * @param isWinner 승리 여부. {@code null}로 지정 시 무승부를 나타냄
     * @return 승패 보정치
     */
    private static double getWinCorrection(Boolean isWinner) {
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
        double averageDiffValue = (EXPECTED_AVERAGE_RANK + averageRR) / 2F - rr;
        int weightValue = mmr - rr;
        return averageDiffValue * 0.04 + weightValue * 0.1;
    }
}
