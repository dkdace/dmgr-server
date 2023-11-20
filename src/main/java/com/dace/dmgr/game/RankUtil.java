package com.dace.dmgr.game;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.lobby.UserData;
import com.dace.dmgr.system.GeneralConfig;
import com.dace.dmgr.system.task.TaskTimer;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 플레이어의 랭킹 관련 기능을 제공하는 클래스.
 */
public final class RankUtil {
    /** 랭크 점수 랭킹 */
    private static List<UserData> rankRateRanking = new ArrayList<>();
    /** 레벨 랭킹 */
    private static List<UserData> levelRanking = new ArrayList<>();

    static {
        new TaskTimer((long) GeneralConfig.RANKING_UPDATE_PERIOD * 60 * 20) {
            @Override
            protected boolean onTimerTick(int i) {
                updateRanking();
                return true;
            }
        };
    }

    /**
     * 모든 랭킹을 업데이트한다.
     */
    private static void updateRanking() {
        File dir = new File(DMGR.getPlugin().getDataFolder(), "User");
        File[] userDataFiles = dir.listFiles();
        if (userDataFiles == null)
            return;

        rankRateRanking = Arrays.stream(userDataFiles)
                .map(file -> new UserData(UUID.fromString(FilenameUtils.removeExtension(file.getName()))))
                .sorted(Comparator.comparing(UserData::getRankRate).reversed())
                .collect(Collectors.toList());
        levelRanking = Arrays.stream(userDataFiles)
                .map(file -> new UserData(UUID.fromString(FilenameUtils.removeExtension(file.getName()))))
                .sorted(Comparator.comparing(UserData::getLevel).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 랭크 점수를 기준으로 내림차순 정렬된 유저 데이터 정보 목록을 반환한다.
     *
     * @param limit 반환할 유저 데이터의 갯수
     */
    public static UserData[] getRankRateRanking(int limit) {
        return rankRateRanking.stream().limit(limit).toArray(UserData[]::new);
    }

    /**
     * 지정한 플레이어의 랭크 점수 순위를 반환한다.
     *
     * @param userData 유저 데이터 정보 객체
     * @return 랭크 점수 순위. 대상이 존재하지 않으면 {@code -1} 반환
     */
    public static int getRankRateRank(UserData userData) {
        int index = rankRateRanking.indexOf(userData);
        return index == -1 ? -1 : index + 1;
    }

    /**
     * 레벨을 기준으로 내림차순 정렬된 유저 데이터 정보 목록을 반환한다.
     *
     * @param limit 반환할 유저 데이터의 갯수
     */
    public static UserData[] getLevelRanking(int limit) {
        return levelRanking.stream().limit(limit).toArray(UserData[]::new);
    }

    /**
     * 지정한 플레이어의 레벨 순위를 반환한다.
     *
     * @param userData 유저 데이터 정보 객체
     * @return 레벨 순위. 대상이 존재하지 않으면 {@code -1} 반환
     */
    public static int getLevelRank(UserData userData) {
        int index = levelRanking.indexOf(userData);
        return index == -1 ? -1 : index + 1;
    }
}
