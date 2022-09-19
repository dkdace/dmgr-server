package com.kiwi.dmgr.match;

import org.bukkit.entity.Player;

public class RankRating {

    public static int getPlayerRankRating(Player p) {
        return 5000;
    }

    public static int getPlayerRankRanking(Player p) {
        return 4;
    }

    public static String getPlayerTierPrefix(Player p) {
        int RR = getPlayerRankRating(p);
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
        int RR = getPlayerRankRating(p);
        if (RR < 0) {
            return "<0";
        } else {
            return String.valueOf(RR);
        }
    }

    /*private static int caculateByScore(Player p, int score) {
        int rr = getPlayerRankRating(p);
    } */
}
