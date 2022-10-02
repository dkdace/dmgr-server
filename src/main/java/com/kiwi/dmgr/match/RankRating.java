package com.kiwi.dmgr.match;

import org.bukkit.entity.Player;

import com.dace.dmgr.user.User;

import java.util.*;

public class RankRating {

    private static final int AVERAGE_RR = 400;
    private static final float AVERAGE_KDA = 2F;
    private static final float MIN_PER_AVERAGE_SCORE = 100F;

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

    // public 랭크 매치 함수
    public static void finishPlayerRankMatch(Player p) {
        int rr = 300;// getPlayerRankRanking(p);
        int mmr = 300;// getPlayerMatchMakingRating(p);
        int changeValue = 2;//getFinalRating();
        //setPlayerRR(p, rr + changeValue);
        //addPlayerMMR(p, mmr);
    }


    // private MMR / RR 기초 함수.
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
