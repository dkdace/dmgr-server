package com.dace.dmgr.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * 밀리초 단위의 기간을 나타내는 클래스.
 *
 * <p>틱 단위와의 호환을 위해 사용한다.</p>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public final class Timespan implements Comparable<Timespan> {
    /** 사용 가능한 최솟값의 기간 */
    public static final Timespan MIN = Timespan.ofMilliseconds(Long.MIN_VALUE);
    /** 사용 가능한 최댓값의 기간 */
    public static final Timespan MAX = Timespan.ofMilliseconds(Long.MAX_VALUE);

    /** 밀리초 : 틱 */
    private static final int MILLISECONDS_TO_TICKS = 50;
    /** 틱 : 초 */
    private static final int TICKS_TO_SECONDS = MILLISECONDS_TO_TICKS * 20;
    /** 초 : 분 */
    private static final int SECONDS_TO_MINUTES = TICKS_TO_SECONDS * 60;
    /** 분 : 시간 */
    private static final int MINUTES_TO_HOURS = TICKS_TO_SECONDS * 60;
    /** 시간 : 일 */
    private static final int HOURS_TO_DAYS = TICKS_TO_SECONDS * 24;

    /** 시간 (ms) */
    private final long milliseconds;

    /**
     * 기간을 반환한다.
     *
     * @param milliseconds 시간 (밀리초)
     * @return 기간
     */
    @NonNull
    public static Timespan ofMilliseconds(long milliseconds) {
        return new Timespan(milliseconds);
    }

    /**
     * 기간을 반환한다.
     *
     * @param ticks 시간 (틱)
     * @return 기간
     */
    @NonNull
    public static Timespan ofTicks(long ticks) {
        return new Timespan(ticks * MILLISECONDS_TO_TICKS);
    }

    /**
     * 기간을 반환한다.
     *
     * @param seconds 시간 (초)
     * @return 기간
     */
    @NonNull
    public static Timespan ofSeconds(double seconds) {
        return new Timespan((long) (seconds * TICKS_TO_SECONDS));
    }

    /**
     * 기간을 반환한다.
     *
     * @param minutes 시간 (분)
     * @return 기간
     */
    @NonNull
    public static Timespan ofMinutes(double minutes) {
        return new Timespan((long) (minutes * SECONDS_TO_MINUTES));
    }

    /**
     * 기간을 반환한다.
     *
     * @param hours 시간 (시간)
     * @return 기간
     */
    @NonNull
    public static Timespan ofHours(double hours) {
        return new Timespan((long) (hours * MINUTES_TO_HOURS));
    }

    /**
     * 기간을 반환한다.
     *
     * @param days 시간 (일)
     * @return 기간
     */
    @NonNull
    public static Timespan ofDays(double days) {
        return new Timespan((long) (days * HOURS_TO_DAYS));
    }

    /**
     * 두 타임스탬프 사이의 기간을 반환한다.
     *
     * @param start 시작 타임스탬프
     * @param end   끝 타임스탬프
     * @return 두 타임스탬프 사이의 기간
     * @see Timestamp#until(Timestamp)
     */
    @NonNull
    public static Timespan between(@NonNull Timestamp start, @NonNull Timestamp end) {
        return start.until(end);
    }

    /**
     * 현재 기간에 지정한 기간을 추가하여 반환한다.
     *
     * @param timespan 추가할 기간
     * @return 새로운 {@link Timespan}
     */
    @NonNull
    public Timespan plus(@NonNull Timespan timespan) {
        if (this.equals(MAX) || timespan.equals(MAX))
            return MAX;
        if (this.equals(MIN) || timespan.equals(MIN))
            return MIN;

        return new Timespan(milliseconds + timespan.milliseconds);
    }

    /**
     * 현재 기간에서 지정한 기간을 차감하여 반환한다.
     *
     * @param timespan 차감할 기간
     * @return 새로운 {@link Timespan}
     * @see Timespan#plus(Timespan)
     */
    @NonNull
    public Timespan minus(@NonNull Timespan timespan) {
        return plus(timespan.negated());
    }

    /**
     * 기간을 음수 값으로 바꿔 반환한다.
     *
     * @return 새로운 {@link Timespan}
     */
    @NonNull
    public Timespan negated() {
        return new Timespan(milliseconds * -1);
    }

    /**
     * 기간이 음수 값인지 확인한다.
     *
     * @return 음수 값 여부
     */
    public boolean isNegative() {
        return milliseconds < 0;
    }

    /**
     * 기간을 밀리초 단위로 반환한다.
     *
     * @return 시간 (밀리초)
     */
    public long toMilliseconds() {
        return milliseconds;
    }

    /**
     * 기간을 틱 단위로 반환한다.
     *
     * @return 시간 (틱)
     */
    public long toTicks() {
        return milliseconds / MILLISECONDS_TO_TICKS;
    }

    /**
     * 기간을 초 단위로 반환한다.
     *
     * @return 시간 (초)
     */
    public double toSeconds() {
        return (double) milliseconds / TICKS_TO_SECONDS;
    }

    /**
     * 기간을 분 단위로 반환한다.
     *
     * @return 시간 (분)
     */
    public double toMinutes() {
        return (double) milliseconds / SECONDS_TO_MINUTES;
    }

    /**
     * 기간을 시간 단위로 반환한다.
     *
     * @return 시간 (시간)
     */
    public double toHours() {
        return (double) milliseconds / MINUTES_TO_HOURS;
    }

    /**
     * 기간을 일 단위로 반환한다.
     *
     * @return 시간 (일)
     */
    public double toDays() {
        return (double) milliseconds / HOURS_TO_DAYS;
    }

    @Override
    public int compareTo(@NonNull Timespan other) {
        return Long.compare(milliseconds, other.milliseconds);
    }

    @Override
    public String toString() {
        return DurationFormatUtils.formatDurationISO(milliseconds);
    }
}
