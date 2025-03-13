package com.dace.dmgr;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

/**
 * 시각(타임스탬프) 을 나타내는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public final class Timestamp implements Comparable<Timestamp> {
    /** 사용 가능한 최솟값의 타임스탬프 */
    public static final Timestamp MIN = new Timestamp(Long.MIN_VALUE);
    /** 사용 가능한 최댓값의 타임스탬프 */
    public static final Timestamp MAX = new Timestamp(Long.MAX_VALUE);

    /** 기록된 시각 (타임스탬프, ms) */
    private final long timestampMillis;

    /**
     * 현재 시각의 타임스탬프를 반환한다.
     *
     * @return 현재 시각의 타임스탬프
     */
    @NonNull
    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 지정한 기간 이후의 타임스탬프를 반환한다.
     *
     * <p>추가할 기간이 0보다 작으면 지정한 기간 이전의 타임스탬프를 반환한다.</p>
     *
     * @param timespan 추가할 기간
     * @return 새로운 {@link Timestamp}
     */
    @NonNull
    public Timestamp plus(@NonNull Timespan timespan) {
        if (this.equals(MAX) || timespan.equals(Timespan.MAX))
            return MAX;
        if (this.equals(MIN))
            return MIN;

        return new Timestamp(timestampMillis + timespan.toMilliseconds());
    }

    /**
     * 타임스탬프의 시점이 지정한 타임스탬프의 시점 이후인지 학인한다.
     *
     * @param timestamp 타임스탬프
     * @return 지정한 타임스탬프의 시점 이후이면 {@code true} 반환
     */
    public boolean isAfter(@NonNull Timestamp timestamp) {
        return compareTo(timestamp) > 0;
    }

    /**
     * 타임스탬프의 시점이 지정한 타임스탬프의 시점 이전인지 학인한다.
     *
     * @param timestamp 타임스탬프
     * @return 지정한 타임스탬프의 시점 이전이면 {@code true} 반환
     */
    public boolean isBefore(@NonNull Timestamp timestamp) {
        return compareTo(timestamp) <= 0;
    }

    /**
     * 타임스탬프의 시점에서 지정한 타임스탬프의 시점까지의 시간을 반환한다.
     *
     * @param timestamp 타임스탬프
     * @return 지정한 타임스탬프까지의 시간. {@code timestamp}가 현재 시점 이전이면 {@link Timespan#ZERO} 반환
     */
    @NonNull
    public Timespan until(@NonNull Timestamp timestamp) {
        return timestampMillis > timestamp.timestampMillis
                ? Timespan.ZERO
                : Timespan.ofMilliseconds(timestamp.timestampMillis - timestampMillis);
    }

    /**
     * 타임스탬프를 Date로 바꿔 반환한다.
     *
     * @return 새로운 {@link Date}
     */
    @NonNull
    public Date toDate() {
        return new Date(timestampMillis);
    }

    @Override
    public int compareTo(@NonNull Timestamp other) {
        return Long.compare(timestampMillis, other.timestampMillis);
    }

    @Override
    public String toString() {
        return DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(timestampMillis);
    }
}
