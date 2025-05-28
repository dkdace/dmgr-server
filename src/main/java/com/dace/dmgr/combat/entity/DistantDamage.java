package com.dace.dmgr.combat.entity;

import org.apache.commons.lang3.Validate;

/**
 * 거리별 피해량을 나타내는 클래스.
 *
 * <p>Example:</p>
 *
 * <pre><code>
 * // 피해량 : 10 ~ 5 (20m~40m)
 * DistantDamage distantDamage = new DistantDamage(10, 20);
 * // 5
 * double damage = distantDamage.getDamage(40);
 * </code></pre>
 *
 * @see DistantTimespan
 */
public final class DistantDamage {
    /** 피해량 */
    private final double damage;
    /** 피해 감소가 시작하는 거리. (단위: 블록) */
    private final double weakeningDistance;

    /**
     * 거리별 피해량 인스턴스를 생성한다.
     *
     * @param damage            피해량. 0 이상의 값
     * @param weakeningDistance 피해 감소가 시작하는 거리. (단위: 블록). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public DistantDamage(double damage, double weakeningDistance) {
        Validate.isTrue(damage >= 0, "damage >= 0 (%f)", damage);
        Validate.isTrue(weakeningDistance >= 0, "weakeningDistance >= 0 (%f)", weakeningDistance);

        this.damage = damage;
        this.weakeningDistance = weakeningDistance;
    }

    /**
     * 지정한 거리에 따른 피해량 감소가 적용된 최종 피해량을 반환한다.
     *
     * @param distance 거리 (단위: 블록). 0 이상의 값
     * @return 최종 피해량
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public double getDamage(double distance) {
        Validate.isTrue(distance >= 0, "distance >= 0 (%f)", distance);

        if (distance <= weakeningDistance)
            return damage;

        double halfDamage = damage / 2.0;
        distance = distance - weakeningDistance;

        return Math.max(halfDamage, halfDamage * ((weakeningDistance - distance) / weakeningDistance) + halfDamage);
    }
}
