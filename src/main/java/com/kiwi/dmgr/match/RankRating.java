package com.kiwi.dmgr.match;

import com.dace.dmgr.lobby.User;
import org.bukkit.entity.Player;

import java.util.*;

import static com.dace.dmgr.system.HashMapList.userMap;

public class RankRating {

    private static final int AVERAGE_RR = 400;
    private static final float AVERAGE_KDA = 2F;
    private static final float MIN_PER_AVERAGE_SCORE = 100F;

    private static final int MAX_MMR_PLAY = 25;
    private static final int MAX_PLACEMENT_PLAY = 5;

    public static int getPlayerRankRanking(Player p) {
        return 4;
    }

    public static String getPlayerTierPrefix(Player p) {
        int RR = 0;//getPlayerRankRating(p);
        String ret = "§f없음";
        if (RR >= 1000) {
            if (getPlayerRankRanking(p) <= 5) {
                ret = "§5네더라이트";
            } else {
                ret = "§b다이아몬드";
            }
        } else {
            if (RR >= 750) return "&a에메랄드";
            if (RR >= 500) return "&c레드스톤";
            if (RR >= 250) return "&e골드";
            if (RR >= 0) return "&7아이언";
            return "&8스톤";
        }

        return ret;
    }

    public static String getPlayerRankRakingDisplay(Player p) {
        int RR = 0;//getRank(p);
        if (RR < 0) {
            return "<0";
        } else {
            return String.valueOf(RR);
        }
    }

    // 랭크 종료 함수
    public static void finishPlayerRankMatch(Player p) {
        int rr = getPlayerRR(p);
        int mmr = getPlayerMMR(p);
        int play = getPlayerPlay(p);
        //setPlayerMMR(p, getFinalMMR(mmr, getExtractedMMR(), play));
        if (!isPlayerRanked(p)) {
            if (play == MAX_PLACEMENT_PLAY) {
                int finalRR = (int) (getPlayerMMR(p) * 0.9);
                if (finalRR > 749) finalRR = 749;
                setPlayerRR(p, finalRR);
                setPlayerRanked(p, true);
            }
        } else {
            int changeValue = 2;//getFinalRating();
            setPlayerRR(p, rr + changeValue);
        }
    }

    // MMR 계산 함수
    private static int getFinalMMR(int preMMR, int addMMR, int play) {
        double finalMMR = 0;
        if (play < MAX_MMR_PLAY) {
            finalMMR = (preMMR * play / (play+1)) + addMMR * (1 / (play+1));
        } else {
            play = MAX_MMR_PLAY;
            finalMMR = (preMMR * (play-1) / (play)) + addMMR * (1 / (play));
        }

        return (int) finalMMR;
    }

    // MMR / RR 불러오기 / 저장 함수
    public static int getPlayerRR(Player p) {
        User user = userMap.get(p);
        return user.getRank();
    }

    public static void setPlayerRR(Player p, int rr) {
        User user = userMap.get(p);
        user.setRank(rr);
    }

    public static int getPlayerMMR(Player p) {
        User user = userMap.get(p);
        return user.getMMR();
    }

    public static void setPlayerMMR(Player p, int mmr) {
        User user = userMap.get(p);
        user.setMMR(mmr);
    }

    public static int getPlayerPlay(Player p) {
        User user = userMap.get(p);
        return user.getRankPlay();
    }

    public static void setPlayerRankPlay(Player p, int play) {
        User user = userMap.get(p);
        user.setRankPlay(play);
    }

    public static boolean isPlayerRanked(Player p) {
        User user = userMap.get(p);
        return user.isRanked();
    }

    public static void setPlayerRanked(Player p, boolean ranked) {
        User user = userMap.get(p);
        user.setRanked(ranked);
    }


    // MMR / RR 계산 함수.
    // playTime 은 초 단위를 기준으로 할 것.
    private static float getKDARating(float kda) {
        float ret = (kda / AVERAGE_KDA) * 20;
        return ret;
    }

    private static float getScoreRating(int score, float playTime) {
        float ret = ((score / MIN_PER_AVERAGE_SCORE) / playTime / 60) * 20;
        return ret;
    }

    private static float getResultRating(String result) {
        if (result.equals("win")) return 10;
        else if (result.equals("lose")) return -8;
        return 0;
    }

    private static float getCorrectRating(int mmr, int rr, int averageRating) {
        float averageDiffValue = (float) (AVERAGE_RR + averageRating) / 2 - rr;
        float weightValue = mmr-rr;
        return (float) (averageDiffValue * 0.04 + weightValue * 0.1);
    }

    private static int getFinalRating(float kda, int score, float playTime, String result, int mmr, int rr, int averageRating) {
        float finalRating = getKDARating(kda) + getScoreRating(score, playTime) +
                getResultRating(result) + getCorrectRating(mmr, rr, averageRating);
        return Math.round(finalRating);
    }

    private static float getExtractedMMR(float kda, int score, float playTime, int averageRating) {
        float midValue = (getKDARating(kda) + getScoreRating(score, playTime)) * 10;
        return (float) Math.pow(midValue, 0.95);
    }
}
