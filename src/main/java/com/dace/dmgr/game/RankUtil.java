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
 * 플레이어의 랭킹 관련 기능을 제공하는 클래스.
 */
public final class RankUtil {
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
