package com.kiwi.dmgr.match;

import com.dace.dmgr.lobby.User;
import com.kiwi.dmgr.game.GameUser;
import com.kiwi.dmgr.game.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

import static com.dace.dmgr.system.HashMapList.userMap;
import static com.kiwi.dmgr.game.GameMapList.gameUserMap;

/**
 * 경쟁 점수와 MMR 기능을 제공하는 클래스
 */
public class RankRating {

    /** 원하는 플레이어의 평균 랭크 레이트를 설정 */
    private static final int AVERAGE_RANK = 400;
    /** 배치로 얻을 수 있는 최대 랭크를 설정 */
    private static final int MAX_PLACEMENT_RANK = 749;
    /** 예상하는 K/DA 평균을 기입 */
    private static final float AVERAGE_KDA = 2F;
    /** 예상하는 1분당 얻는 점수를 기입 */
    private static final float MIN_PER_AVERAGE_SCORE = 100F;

    /** MMR 수치에 영향을 미치는 전회 플레이 횟수를 설정 */
    private static final int MAX_MMR_PLAY = 25;
    /** 랭크가 결정되는 배치 판 수를 설정 */
    private static final int MAX_PLACEMENT_PLAY = 5;

    /**
     * 플레이어의 랭크 등수를 반환
     *
     * @return 랭크 등수
     */
    public int getPlayerankankPlace(Player player) {
        return 10;
    }

    /**
     * 플레이어의 랭크 접두사(칭호)를 반환
     *
     * @param player 플레이어
     * @return 플레이어 랭크 접두사
     */
    public String getPlayerTierPrefix(Player player) {
        User user = userMap.get(player);
        int rank = user.getRank();
        String ret = "§f없음";
        if (rank >= 1000) {
            if (getPlayerankankPlace(player) <= 5) {
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
     * 플레이어의 표시되는 랭크 레이팅을 반환
     * 0 미만이면 <0 으로 표시됨
     *
     * @param player 플레이어
     * @return 표시되는 랭크 레이팅
     */
    public String getPlayerankankDisplay(Player player) {
        User user = userMap.get(player);
        int rank = user.getRank();
        if (rank < 0) {
            return "<0";
        } else {
            return String.valueOf(rank);
        }
    }

    /**
     * 랭크 매치 종료후 플레이어의 랭크와 MMR 을 변동시킨다.
     *
     * <p>
     * 랭크 배치 중이면 플레이어의 랭크 레이팅을 MMR * 0.9로 결정
     * 배치되는 랭크가 750 이상으로 넘어갈 수 없음
     * </p>
     *
     * @param player 플레이어
     */
    public static void updateRankAfterGame(Player player, Team winTeam) {
        User user = userMap.get(player);
        GameUser gameUser = gameUserMap.get(player);

        int rank = user.getRank();
        int mmr = user.getMMR();
        int play = user.getRankPlay();
        boolean isRanked = user.isRanked();

        float kda = gameUser.getKDA();
        int score = gameUser.getScore();
        int playTime = gameUser.getGame().getPlayTime();
        int gameAverageMMR = (int) gameUser.getGame().getAverageIndicator("MMR");
        int gameAverageRank = (int) gameUser.getGame().getAverageIndicator("Rank");

        Team team = gameUser.getTeam();

        user.setMMR(getFinalMMR(mmr, getExtractedMMR(kda, score, playTime, gameAverageMMR), play));

        if (!isRanked) {
            if (play == MAX_PLACEMENT_PLAY) {
                int finalRank = (int) (mmr * 0.9);
                if (finalRank > MAX_PLACEMENT_RANK) finalRank = MAX_PLACEMENT_RANK;
                user.setRank(finalRank);
                user.setRanked(true);
            }

        } else {
            int changeValue = getFinalRating(kda, score, playTime, team, winTeam, mmr, rank, gameAverageRank);
            user.setRank(rank + changeValue);
        }
    }

    /**
     * 게임 종료 후 결과에 따라 플레이어의 MMR 만 변동시킨다.
     *
     * @param player 플레이어
     */
    public static void updateMMRAfterGame(Player player) {
        User user = userMap.get(player);
        GameUser gameUser = gameUserMap.get(player);

        int mmr = user.getMMR();
        int play = user.getMMRPlay();

        float kda = gameUser.getKDA();
        int score = gameUser.getScore();
        int playTime = gameUser.getGame().getPlayTime();
        int gameAverageMMR = (int) gameUser.getGame().getAverageIndicator("MMR");

        user.setMMR(getFinalMMR(mmr, getExtractedMMR(kda, score, playTime, gameAverageMMR), play));
        user.setMMRPlay(play+1);
        Bukkit.getConsoleSender().sendMessage("[RankSystem] 유저 MMR 변동됨: (" + player.getName() + ") " + mmr + " -> " + user.getMMR() + " MMR PLAY: " + play);
    }

    /**
     * 경기를 마치고 나서 얻은 MMR 을 기존 MMR 과 결합하여 최종 MMR 을 반환
     *
     * @param preMMR 기존 MMR
     * @param addMMR 추가 MMR
     * @param play 플레이 횟수
     * @return MMR
     */
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
     * KDA 랭크 레이팅을 반환한다.
     *
     * @param kda KDA
     * @return KDA RR
     */
    private static float getKDARating(float kda) {
        return (kda / AVERAGE_KDA) * 20;
    }

    /**
     * 게임 점수 랭크 레이팅을 반환한다.
     *
     * @param score 점수
     * @param playTime 플레이 시간
     * @return 게임 점수 RR
     */
    private static float getScoreRating(int score, int playTime) {
        return ((score / MIN_PER_AVERAGE_SCORE) / playTime / 60) * 20;
    }

    /**
     * 게임 결과 랭크 레이팅을 반환한다.
     *
     * @param team 팀
     * @param winTeam 승리한 팀
     * @return 게임 결과 RR
     */
    private static float getResultRating(Team team, Team winTeam) {
        if (team == winTeam) return 10;
        else if (winTeam != Team.NONE) return -8;
        return 0;
    }

    /**
     * 게임 보정치 레이팅을 반환한다.
     *
     * @param mmr MMR
     * @param rank RR
     * @param averageRR 평균 RR
     * @return 보정치 RR
     */
    private static float getCorrectRating(int mmr, int rank, int averageRR) {
        float averageDiffValue = (float) (AVERAGE_RANK + averageRR) / 2 - rank;
        float weightValue = mmr-rank;
        return (float) (averageDiffValue * 0.04 + weightValue * 0.1);
    }

    /**
     * 결과 레이팅을 반환한다.
     *
     * @param kda KDA
     * @param score 점수
     * @param playTime 플레이 시간
     * @param team 팀
     * @param winTeam 승리한 팀
     * @param mmr MMR
     * @param rank RR
     * @param averageRR 평균 RR
     * @return 결과 RR
     */
    private static int getFinalRating(float kda, int score, int playTime, Team team, Team winTeam, int mmr, int rank, int averageRR) {
        float finalRating = getKDARating(kda) + getScoreRating(score, playTime) +
                getResultRating(team, winTeam) + getCorrectRating(mmr, rank, averageRR);
        return Math.round(finalRating);
    }

    /**
     * 게임 성적으로 추정되는 MMR을 반환한다.
     *
     * @param kda KDA
     * @param score 점수
     * @param playTime 플레이 시간
     * @param averageMMR 평균 MMR
     * @return 추정 MMR
     */
    private static int getExtractedMMR(float kda, int score, int playTime, int averageMMR) {
        float midValue = (getKDARating(kda) + getScoreRating(score, playTime)) * 10;
        int returnValue = (int) (midValue + averageMMR);
        return (Math.min(returnValue, 1000));
    }
}
