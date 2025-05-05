package com.dace.dmgr;

import com.dace.dmgr.yaml.Serializer;
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
    /** 사용 가능한 최댓값의 기간. 무한대로 취급 */
    public static final Timespan MAX = new Timespan(Long.MAX_VALUE);

    /** 밀리초로 나타낸 1틱 */
    private static final int MILLISECONDS_IN_TICK = 50;
    /** 밀리초로 나타낸 1초 */
    private static final int MILLISECONDS_IN_SECOND = MILLISECONDS_IN_TICK * 20;
    /** 밀리초로 나타낸 1분 */
    private static final int MILLISECONDS_IN_MINUTE = MILLISECONDS_IN_SECOND * 60;
    /** 밀리초로 나타낸 1시간 */
    private static final int MILLISECONDS_IN_HOUR = MILLISECONDS_IN_MINUTE * 60;
    /** 밀리초로 나타낸 1일 */
    private static final int MILLISECONDS_IN_DAY = MILLISECONDS_IN_HOUR * 24;

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
     * @param milliseconds 시간 (밀리초). 0 이상의 값
     * @return 기간
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static Timespan ofMilliseconds(double milliseconds) {
        Validate.isTrue(milliseconds >= 0.0, "milliseconds >= 0 (%f)", milliseconds);
        Validate.notNaN(milliseconds, "milliseconds is not NaN");

        if (milliseconds < 1.0)
            return ZERO;
        if (milliseconds >= Long.MAX_VALUE)
            return MAX;

        return new Timespan((long) milliseconds);
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
        return ofMilliseconds(ticks * MILLISECONDS_IN_TICK);
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
        return ofMilliseconds(seconds * MILLISECONDS_IN_SECOND);
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
        return ofMilliseconds(minutes * MILLISECONDS_IN_MINUTE);
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
        return ofMilliseconds(hours * MILLISECONDS_IN_HOUR);
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
        return ofMilliseconds(days * MILLISECONDS_IN_DAY);
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
        return this.equals(MAX) ? MAX : ofMilliseconds(Math.min(milliseconds * value, Long.MAX_VALUE));
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
        return this.equals(MAX) ? MAX : ofMilliseconds(Math.min(milliseconds / value, Long.MAX_VALUE));
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
     * 값이 무한대인지 확인한다.
     *
     * @return {@link Timespan#MAX}와 같으면 {@code true} 반환
     */
    public boolean isInfinity() {
        return equals(MAX);
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
        return milliseconds / MILLISECONDS_IN_TICK;
    }

    /**
     * 기간을 초 단위로 반환한다.
     *
     * @return 시간 (초)
     */
    public double toSeconds() {
        return (double) milliseconds / MILLISECONDS_IN_SECOND;
    }

    /**
     * 기간을 분 단위로 반환한다.
     *
     * @return 시간 (분)
     */
    public double toMinutes() {
        return (double) milliseconds / MILLISECONDS_IN_MINUTE;
    }

    /**
     * 기간을 시간 단위로 반환한다.
     *
     * @return 시간 (시간)
     */
    public double toHours() {
        return (double) milliseconds / MILLISECONDS_IN_HOUR;
    }

    /**
     * 기간을 일 단위로 반환한다.
     *
     * @return 시간 (일)
     */
    public double toDays() {
        return (double) milliseconds / MILLISECONDS_IN_DAY;
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
     * {@link Timespan}의 직렬화 처리기 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class TimespanSerializer implements Serializer<Timespan, Map<String, Number>> {
        @Getter
        private static final TimespanSerializer instance = new TimespanSerializer();

        @Override
        @NonNull
        public Map<String, Number> serialize(@NonNull Timespan value) {
            Map<String, Number> map = new HashMap<>();

            if (value.equals(MAX)) {
                map.put("infinity", 1);
                return map;
            }

            long milliseconds = value.milliseconds;
            long ticks = milliseconds / 50;
            milliseconds %= 50;
            long seconds = ticks / 20;
            ticks %= 20;
            long minutes = seconds / 60;
            seconds %= 60;
            long hours = minutes / 60;
            minutes %= 60;
            long days = hours / 24;
            hours %= 24;

            if (days > 0)
                map.put("days", days);
            if (hours > 0)
                map.put("hours", hours);
            if (minutes > 0)
                map.put("minutes", minutes);
            if (seconds > 0)
                map.put("seconds", seconds);
            if (ticks > 0)
                map.put("ticks", ticks);
            if (milliseconds > 0)
                map.put("milliseconds", milliseconds);

            return map;
        }

        @Override
        @NonNull
        public Timespan deserialize(@NonNull Map<String, Number> value) {
            if (value.containsKey("infinity"))
                return MAX;

            return ofDays(NumberConversions.toDouble(value.get("days")))
                    .plus(ofHours(NumberConversions.toDouble(value.get("hours"))))
                    .plus(ofMinutes(NumberConversions.toDouble(value.get("minutes"))))
                    .plus(ofSeconds(NumberConversions.toDouble(value.get("seconds"))))
                    .plus(ofTicks(NumberConversions.toLong(value.get("ticks"))));
        }
    }
}
