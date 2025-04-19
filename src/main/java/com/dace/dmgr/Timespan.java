package com.dace.dmgr;

import lombok.*;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.util.NumberConversions;

import java.util.HashMap;
import java.util.Map;

/**
 * 밀리초 단위의 기간을 나타내는 클래스.
 *
 * <p>음수 값을 가질 수 없으며, 틱 단위와의 호환을 위해 사용한다.</p>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public final class Timespan implements Comparable<Timespan> {
    /** 0의 값을 가진 기간 */
    public static final Timespan ZERO = new Timespan(0);
    /** 사용 가능한 최댓값의 기간 */
    public static final Timespan MAX = new Timespan(Long.MAX_VALUE);

    /** 밀리초 : 틱 */
    private static final int MILLISECONDS_TO_TICKS = 50;
    /** 틱 : 초 */
    private static final int TICKS_TO_SECONDS = MILLISECONDS_TO_TICKS * 20;
    /** 초 : 분 */
    private static final int SECONDS_TO_MINUTES = TICKS_TO_SECONDS * 60;
    /** 분 : 시간 */
    private static final int MINUTES_TO_HOURS = SECONDS_TO_MINUTES * 60;
    /** 시간 : 일 */
    private static final int HOURS_TO_DAYS = MINUTES_TO_HOURS * 24;

    /** 시간 (ms) */
    private final long milliseconds;

    /**
     * 기간을 반환한다.
     *
     * @param milliseconds 시간 (밀리초). 0 이상의 값
     * @return 기간
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static Timespan ofMilliseconds(long milliseconds) {
        Validate.isTrue(milliseconds >= 0, "milliseconds >= 0 (%d)", milliseconds);

        if (milliseconds == 0)
            return ZERO;
        if (milliseconds == Long.MAX_VALUE)
            return MAX;

        return new Timespan(milliseconds);
    }

    /**
     * 기간을 반환한다.
     *
     * @param ticks 시간 (틱). 0 이상의 값
     * @return 기간
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static Timespan ofTicks(long ticks) {
        return ofMilliseconds(ticks * MILLISECONDS_TO_TICKS);
    }

    /**
     * 기간을 반환한다.
     *
     * @param seconds 시간 (초). 0 이상의 값
     * @return 기간
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static Timespan ofSeconds(double seconds) {
        return ofMilliseconds((long) (seconds * TICKS_TO_SECONDS));
    }

    /**
     * 기간을 반환한다.
     *
     * @param minutes 시간 (분). 0 이상의 값
     * @return 기간
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static Timespan ofMinutes(double minutes) {
        return ofMilliseconds((long) (minutes * SECONDS_TO_MINUTES));
    }

    /**
     * 기간을 반환한다.
     *
     * @param hours 시간 (시간). 0 이상의 값
     * @return 기간
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static Timespan ofHours(double hours) {
        return ofMilliseconds((long) (hours * MINUTES_TO_HOURS));
    }

    /**
     * 기간을 반환한다.
     *
     * @param days 시간 (일). 0 이상의 값
     * @return 기간
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static Timespan ofDays(double days) {
        return ofMilliseconds((long) (days * HOURS_TO_DAYS));
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
     * @see Timespan#minus(Timespan)
     */
    @NonNull
    public Timespan plus(@NonNull Timespan timespan) {
        if (this.equals(MAX) || timespan.equals(MAX))
            return MAX;

        return ofMilliseconds(milliseconds + timespan.milliseconds);
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
        if (this.equals(MAX) || timespan.equals(MAX))
            return MAX;

        return ofMilliseconds(Math.max(0, milliseconds - timespan.milliseconds));
    }

    /**
     * 현재 기간에 지정한 값만큼 곱하여 반환한다.
     *
     * @param value 곱할 값
     * @return 새로운 {@link Timespan}
     * @see Timespan#divide(double)
     */
    @NonNull
    public Timespan multiply(double value) {
        return this.equals(MAX) ? MAX : ofMilliseconds((long) (milliseconds * value));
    }

    /**
     * 현재 기간에서 지정한 값을 나누어 반환한다.
     *
     * @param value 나눌 값
     * @return 새로운 {@link Timespan}
     * @see Timespan#multiply(double)
     */
    @NonNull
    public Timespan divide(double value) {
        return this.equals(MAX) ? MAX : ofMilliseconds((long) (milliseconds / value));
    }

    /**
     * 값이 0인지 확인한다.
     *
     * @return {@link Timespan#ZERO}와 같으면 {@code true} 반환
     */
    public boolean isZero() {
        return equals(ZERO);
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

    /**
     * <p>직렬화 형식:</p>
     *
     * <table>
     * <tr><th>키</th><th>값</th><th>예시</th></tr>
     * <tr><td>hours</td><td>{@link Timespan#ofHours(double)}</td><td>1.0</td></tr>
     * <tr><td>minutes</td><td>{@link Timespan#ofMinutes(double)}</td><td>1.0</td></tr>
     * <tr><td>seconds</td><td>{@link Timespan#ofSeconds(double)}</td><td>1.0</td></tr>
     * <tr><td>ticks</td><td>{@link Timespan#ofTicks(long)}</td><td>10</td></tr>
     * </table>
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Serializer implements YamlFile.Serializer<Timespan, Map<String, Number>> {
        @Getter
        private static final Serializer instance = new Serializer();

        @Override
        @NonNull
        public Map<String, Number> serialize(@NonNull Timespan value) {
            Map<String, Number> map = new HashMap<>();
            map.put("ticks", value.toTicks());

            return map;
        }

        @Override
        @NonNull
        public Timespan deserialize(@NonNull Map<String, Number> value) {
            return ofHours(NumberConversions.toDouble(value.get("hours")))
                    .plus(ofMinutes(NumberConversions.toDouble(value.get("minutes"))))
                    .plus(ofSeconds(NumberConversions.toDouble(value.get("seconds"))))
                    .plus(ofTicks(NumberConversions.toLong(value.get("ticks"))));
        }
    }
}
