package com.kiwi.dmgr.match;

import com.dace.dmgr.lobby.User;
import org.bukkit.entity.Player;

import java.util.*;

import static com.dace.dmgr.system.HashMapList.userMap;

public class RankRating {

    /** 원하는 플레이어의 평균 랭크 레이트를 설정 */
    private static final int AVERAGE_RR = 400;
    /** 예상하는 K/DA 평균을 기입 */
    private static final float AVERAGE_KDA = 2F;
    /** 예상하는 1분당 얻는 점수를 기입 */
    private static final float MIN_PER_AVERAGE_SCORE = 100F;

    /** MMR 수치에 영향을 미치는 전회 플레이 횟수를 설정 */
    private static final int MAX_MMR_PLAY = 25;
    /** 랭크가 결정되는 배치 판 수를 설정 */
    private static final int MAX_PLACEMENT_PLAY = 5;

    /**
     *  플레이어의 랭크 등수를 반환
     *
     *  @return 랭크 등수 */
    public static int getPlayerRankPlace(Player player) {
        User user = userMap.get(player);
        return user.getRank();
    }

    /**
     *  플레이어의 랭크 접두사(칭호)를 반환
     *
     *  @param player 플레이어
     *  @return 플레이어 랭크 접두사 */
    public static String getPlayerTierPrefix(Player player) {
        User user = userMap.get(player);
        int rank = user.getRank();
        String ret = "§f없음";
        if (rank >= 1000) {
            if (getPlayerRankPlace(player) <= 5) {
                ret = "§5네더라이트";
            } else {
                ret = "§b다이아몬드";
            }
        } else {
            if (rank >= 750) return "&a에메랄드";
            if (rank >= 500) return "&c레드스톤";
            if (rank >= 250) return "&e골드";
            if (rank >= 0) return "&7아이언";
            return "&8스톤";
        }

        return ret;
    }

    /**
     *  플레이어의 표시되는 랭크 레이팅을 반환
     *  0 미만이면 <0 으로 표시됨
     *
     *  @param player 플레이어
     *  @return 표시되는 랭크 레이팅 */
    public static String getPlayerRankRakingDisplay(Player player) {
        User user = userMap.get(player);
        int rank = user.getRank();
        if (rank < 0) {
            return "<0";
        } else {
            return String.valueOf(rank);
        }
    }

    /**
     *  랭크 배치가 종료된 후 플레어의 랭크 레이팅을 결정
     *  랭크 레이팅은 MMR * 0.9로 설정
     *  배치되는 랭크가 750 이상으로 넘어갈 수 없음
     *
     *  @param player 플레이어 */
    public static void finishPlayerRankMatch(Player player) {
        User user = userMap.get(player);
        int rank = user.getRank();
        int mmr = user.getMMR();
        int play = user.getRankPlay();
        boolean isRanked = user.isRanked();
        //setPlayerMMR(player, getFinalMMR(mmr, getExtractedMMR(), play));
        if (!isRanked) {
            if (play == MAX_PLACEMENT_PLAY) {
                int finalRank = (int) (mmr * 0.9);
                if (finalRank > 749) finalRank = 749;
                user.setRank(finalRank);
                user.setRanked(true);
            }
        } else {
            int changeValue = 2;//getFinalRating();
            user.setRank(rank + changeValue);
        }
    }

    /**
     *  경기를 마치고 나서 얻은 MMR 을 기존 MMR 과 결합하여 최종 MMR 을 반환
     *
     *  @param preMMR 기존 MMR
     *  @param addMMR 추가 MMR
     *  @param play 플레이 횟수
     *  @return MMR */
    private static int getFinalMMR(int preMMR, int addMMR, int play) {
        double finalMMR;
        if (play < MAX_MMR_PLAY) {
            finalMMR = (preMMR * play / (play+1F)) + addMMR * (1F / (play+1));
        } else {
            play = MAX_MMR_PLAY;
            finalMMR = (preMMR * (play-1F) / (play)) + addMMR * (1F / (play));
        }

        return (int) finalMMR;
    }

    /**
    public static int getPlayerRR(Player player) {
        User user = userMap.get(player);
        return user.getRank();
    }

    public static void setPlayerRR(Player player, int rank) {
        User user = userMap.get(player);
        user.setRank(rank);
    }

    public static int getPlayerMMR(Player player) {
        User user = userMap.get(player);
        return user.getMMR();
    }

    public static void setPlayerMMR(Player player, int mmr) {
        User user = userMap.get(player);
        user.setMMR(mmr);
    }

    public static int getPlayerPlay(Player player) {
        User user = userMap.get(player);
        return user.getRankPlay();
    }

    public static void setPlayerRankPlay(Player player, int play) {
        User user = userMap.get(player);
        user.setRankPlay(play);
    }

    public static boolean isPlayerRanked(Player player) {
        User user = userMap.get(player);
        return user.isRanked();
    }

    public static void setPlayerRanked(Player player, boolean ranked) {
        User user = userMap.get(player);
        user.setRanked(ranked);
    }
     */


    // MMR / RR 계산 함수.
    // playTime 은 초 단위를 기준으로 할 것.
    private float getKDARating(float kda) {
        return (kda / AVERAGE_KDA) * 20;
    }

    private float getScoreRating(int score, float playTime) {
        return ((score / MIN_PER_AVERAGE_SCORE) / playTime / 60) * 20;
    }

    private float getResultRating(String result) {
        if (result.equals("win")) return 10;
        else if (result.equals("lose")) return -8;
        return 0;
    }

    private float getCorrectRating(int mmr, int rr, int averageRating) {
        float averageDiffValue = (float) (AVERAGE_RR + averageRating) / 2 - rr;
        float weightValue = mmr-rr;
        return (float) (averageDiffValue * 0.04 + weightValue * 0.1);
    }

    private int getFinalRating(float kda, int score, float playTime, String result, int mmr, int rr, int averageRating) {
        float finalRating = getKDARating(kda) + getScoreRating(score, playTime) +
                getResultRating(result) + getCorrectRating(mmr, rr, averageRating);
        return Math.round(finalRating);
    }

    private float getExtractedMMR(float kda, int score, float playTime, int averageRating) {
        float midValue = (getKDARating(kda) + getScoreRating(score, playTime)) * 10;
        return (float) Math.pow(midValue, 0.95);
    }
}
