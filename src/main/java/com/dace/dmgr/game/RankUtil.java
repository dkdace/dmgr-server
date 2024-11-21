package com.dace.dmgr.game;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;

/**
 * 플레이어의 랭킹 관련 기능을 제공하는 클래스.
 */
@UtilityClass
public final class RankUtil {
    /** 랭킹 (데이터 지표 : 유저 데이터 목록) */
    private static final EnumMap<Indicator, UserData[]> ranking = new EnumMap<>(Indicator.class);

    /**
     * 랭킹 업데이트 스케쥴러를 실행한다.
     */
    public static void run() {
        new IntervalTask(i -> {
            ConsoleLogger.info("전체 랭킹 업데이트 중...");

            updateRanking()
                    .onFinish(length -> {
                        ConsoleLogger.info("전체 랭킹 업데이트 완료, 유저 수 : {0}", length);
                    })
                    .onError(ex -> ConsoleLogger.severe("전체 랭킹 업데이트 실패", ex));

            return true;
        }, GeneralConfig.getConfig().getRankingUpdatePeriodMinutes() * 60 * 20L);
    }

    /**
     * 모든 랭킹을 업데이트한다.
     *
     * @return 전체 유저 수
     */
    @NonNull
    private static AsyncTask<@NonNull Integer> updateRanking() {
        return new AsyncTask<>((onFinish, onError) -> {
            try {
                Collection<UserData> userDatas = UserData.getAllUserDatas();
                if (userDatas.isEmpty()) {
                    onFinish.accept(0);
                    return;
                }

                ranking.put(Indicator.RANK_RATE, userDatas.stream().sorted(Comparator.comparing(UserData::getRankRate).reversed())
                        .toArray(UserData[]::new));
                ranking.put(Indicator.LEVEL, userDatas.stream().sorted(Comparator.comparing(UserData::getLevel).reversed())
                        .toArray(UserData[]::new));

                onFinish.accept(userDatas.size());
            } catch (Exception ex) {
                onError.accept(ex);
            }
        });
    }

    /**
     * 랭크 점수를 기준으로 내림차순 정렬된 유저 데이터 정보 목록을 반환한다.
     *
     * @param indicator 데이터 지표 종류
     * @param limit     반환할 유저 데이터의 갯수. 1 이상의 값
     * @return {@code limit}위까지의 유저 랭킹
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static UserData @NonNull [] getRanking(@NonNull RankUtil.Indicator indicator, int limit) {
        if (limit < 1)
            throw new IllegalArgumentException("'limit'가 1 이상이어야 함");

        UserData[] userDatas = ranking.get(indicator);
        if (userDatas == null)
            return new UserData[0];

        return Arrays.copyOf(userDatas, Math.min(userDatas.length, limit));
    }

    /**
     * 지정한 플레이어의 랭크 점수 순위를 반환한다.
     *
     * @param indicator 데이터 지표 종류
     * @param userData  유저 데이터 정보 객체
     * @return 랭크 점수 순위. 대상이 존재하지 않으면 -1 반환
     */
    public static int getRankIndex(@NonNull RankUtil.Indicator indicator, @NonNull UserData userData) {
        UserData[] userDatas = ranking.get(indicator);
        if (userDatas == null)
            return -1;

        for (int i = 0; i < userDatas.length; i++) {
            if (userDatas[i] == userData)
                return i;
        }

        return -1;
    }

    /**
     * 랭킹에 사용되는 데이터 지표 목록.
     */
    public enum Indicator {
        /** 랭크 점수 */
        RANK_RATE,
        /** 레벨 */
        LEVEL
    }
}
