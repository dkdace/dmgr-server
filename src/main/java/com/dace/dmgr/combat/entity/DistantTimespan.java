package com.dace.dmgr.combat.entity;

import com.dace.dmgr.Timespan;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

/**
 * 거리별 기간을 나타내는 클래스.
 *
 * <p>Example:</p>
 *
 * <pre><code>
 * // 기간 : 10초 ~ 5초 (20m~40m)
 * DistantTimespan distantTimespan = new DistantTimespan(Timespan.ofSeconds(10), 20);
 * // 5초
 * Timespan duration = distantTimespan.getTimespan(40);
 * </code></pre>
 */
public final class DistantTimespan {
    /** 기간 */
    private final Timespan timespan;
    /** 피해 감소가 시작하는 거리. (단위: 블록) */
    private final double weakeningDistance;

    /**
     * 거리별 기간 인스턴스를 생성한다.
     *
     * @param timespan          기간
     * @param weakeningDistance 시간 감소가 시작하는 거리. (단위: 블록). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public DistantTimespan(@NonNull Timespan timespan, double weakeningDistance) {
        Validate.isTrue(weakeningDistance >= 0, "weakeningDistance >= 0 (%f)", weakeningDistance);

        this.timespan = timespan;
        this.weakeningDistance = weakeningDistance;
    }

    /**
     * 지정한 거리에 따른 시간 감소가 적용된 최종 기간을 반환한다.
     *
     * @param distance 거리 (단위: 블록). 0 이상의 값
     * @return 최종 기간
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public Timespan getTimespan(double distance) {
        Validate.isTrue(distance >= 0, "distance >= 0 (%f)", distance);

        if (distance <= weakeningDistance)
            return timespan;

        Timespan halfDamage = timespan.divide(2);
        distance = distance - weakeningDistance;

        Timespan result = halfDamage.multiply((weakeningDistance - distance) / weakeningDistance).plus(halfDamage);
        return halfDamage.compareTo(result) > 0 ? halfDamage : result;
    }
}
