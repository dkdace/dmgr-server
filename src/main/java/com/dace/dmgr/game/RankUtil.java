package com.dace.dmgr.game;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.lobby.UserData;
import com.dace.dmgr.system.task.TaskTimer;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

/**
 * 플레이어의 MMR 및 랭크 점수 관련 기능을 제공하는 클래스.
 */
public final class RankUtil {
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

    /** 랭크 점수 랭킹 */
    private static UserData[] rankRateRanking = null;
    /** 레벨 랭킹 */
    private static UserData[] levelRanking = null;

    /**
     * 랭크 점수를 기준으로 내림차순 정렬된 유저 데이터 정보 목록을 반환한다.
     *
     * @param limit 반환할 유저 데이터의 갯수
     */
    public static UserData[] getRankRateRanking(int limit) {
        return Arrays.stream(rankRateRanking).limit(limit).toArray(UserData[]::new);
    }

    /**
     * 지정한 플레이어의 랭크 점수 순위를 반환한다.
     *
     * @param userData 유저 데이터 정보 객체
     * @return 랭크 점수 순위. 대상이 존재하지 않으면 {@code -1} 반환
     */
    public static int getRankRateRank(UserData userData) {
        int index = Arrays.binarySearch(rankRateRanking, userData);
        return index < 0 ? -1 : index + 1;
    }

    /**
     * 레벨을 기준으로 내림차순 정렬된 유저 데이터 정보 목록을 반환한다.
     *
     * @param limit 반환할 유저 데이터의 갯수
     */
    public static UserData[] getLevelRanking(int limit) {
        return Arrays.stream(levelRanking).limit(limit).toArray(UserData[]::new);
    }

    /**
     * 지정한 플레이어의 레벨 순위를 반환한다.
     *
     * @param userData 유저 데이터 정보 객체
     * @return 레벨 순위. 대상이 존재하지 않으면 {@code -1} 반환
     */
    public static int getLevelRank(UserData userData) {
        int index = Arrays.binarySearch(levelRanking, userData);
        return index < 0 ? -1 : index + 1;
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

    /**
     * 일정 주기마다 랭킹을 업데이트 해주는 클래스.
     */
    public static final class RankUpdater {
        /** 랭킹 업데이트 주기 (분) */
        private static final int UPDATE_PERIOD = 5;

        public static void init() {
            new TaskTimer((long) UPDATE_PERIOD * 60 * 20) {
                @Override
                protected boolean onTimerTick(int i) {
                    File dir = new File(DMGR.getPlugin().getDataFolder(), "User");
                    File[] userDataFiles = dir.listFiles();

                    rankRateRanking = Arrays.stream(userDataFiles)
                            .map(file -> new UserData(UUID.fromString(FilenameUtils.removeExtension(file.getName()))))
                            .sorted(Comparator.comparing(UserData::getRankRate).reversed())
                            .toArray(UserData[]::new);
                    levelRanking = Arrays.stream(userDataFiles)
                            .map(file -> new UserData(UUID.fromString(FilenameUtils.removeExtension(file.getName()))))
                            .sorted(Comparator.comparing(UserData::getLevel).reversed())
                            .toArray(UserData[]::new);

                    return true;
                }
            };
        }
    }
}
