package com.dace.dmgr.user;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * 게임 랭킹 관련 기능을 제공하는 클래스.
 */
public final class RankManager {
    @Getter
    private static final RankManager instance = new RankManager();
    /** 랭킹 항목별 유저 데이터 목록 (랭킹 항목 : 유저 데이터 목록) */
    private final EnumMap<RankType, List<UserData>> rankingMap = new EnumMap<>(RankType.class);

    /**
     * 랭킹 업데이트 스케쥴러를 실행한다.
     */
    private RankManager() {
        new IntervalTask(i -> {
            ConsoleLogger.info("전체 랭킹 업데이트 중...");

            updateRanking()
                    .onFinish((Consumer<Integer>) length -> ConsoleLogger.info("전체 랭킹 업데이트 완료, 유저 수 : {0}", length))
                    .onError(ex -> ConsoleLogger.severe("전체 랭킹 업데이트 실패", ex));
        }, GeneralConfig.getConfig().getRankingUpdatePeriod().toTicks());
    }

    /**
     * 모든 랭킹을 업데이트한다.
     *
     * @return 전체 유저 수
     */
    @NonNull
    private AsyncTask<@NonNull Integer> updateRanking() {
        return AsyncTask.create(() -> {
            Collection<UserData> userDatas = UserData.getAllUserDatas();
            if (userDatas.isEmpty())
                return 0;

            for (RankType rankType : RankType.values())
                rankingMap.put(rankType, userDatas.stream()
                        .sorted(Comparator.comparing(rankType.valueFunction::applyAsInt).reversed())
                        .collect(Collectors.toList()));

            return userDatas.size();
        });
    }

    /**
     * 지정한 랭킹 항목을 기준으로 내림차순 정렬된 유저 데이터 정보 목록을 반환한다.
     *
     * @param rankType 랭킹 항목
     * @param limit    반환할 유저 데이터의 갯수. 1 이상의 값
     * @return {@code limit}위까지의 유저 랭킹
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    @UnmodifiableView
    public List<@NonNull UserData> getRanking(@NonNull RankManager.RankType rankType, int limit) {
        Validate.isTrue(limit >= 1, "limit >= 1 (%d)", limit);

        List<UserData> userDatas = rankingMap.get(rankType);
        if (userDatas == null)
            userDatas = Collections.emptyList();

        userDatas = userDatas.subList(0, Math.min(userDatas.size(), limit));

        return Collections.unmodifiableList(userDatas);
    }

    /**
     * 지정한 플레이어의 랭킹 순위를 반환한다.
     *
     * @param rankType 랭킹 항목
     * @param userData 유저 데이터 정보 인스턴스
     * @return 랭킹 순위. 대상이 존재하지 않으면 -1 반환
     */
    public int getRankIndex(@NonNull RankManager.RankType rankType, @NonNull UserData userData) {
        List<UserData> userDatas = rankingMap.get(rankType);
        if (userDatas == null)
            return -1;

        return userDatas.indexOf(userData);
    }

    /**
     * 랭킹에 사용되는 데이터 항목 목록.
     */
    @AllArgsConstructor
    public enum RankType {
        /** 랭크 점수 */
        RANK_RATE(UserData::getRankRate),
        /** 레벨 */
        LEVEL(UserData::getLevel);

        /** 항목 값 반환에 실행할 작업 */
        private final ToIntFunction<UserData> valueFunction;
    }
}
