package com.dace.dmgr.util.location;

import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 반복 가능한 이동하는 위치를 나타내는 클래스.
 */
public final class IterableLocation implements Iterable<Location> {
    /** 시작 위치 */
    private final Location location;
    /** 시작 속도 */
    private final Vector velocity;
    /** 최대 이동 거리. (단위: 블록) */
    private final double maxTravelDistance;

    /**
     * 기준 위치에서 지정한 속도로 이동하는 반복 가능한 위치 인스턴스를 생성한다.
     *
     * @param location          기준 위치
     * @param velocity          속도
     * @param maxTravelDistance 최대 이동 거리. (단위: 블록). 0 이상의 값.
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    IterableLocation(@NonNull Location location, @NonNull Vector velocity, double maxTravelDistance) {
        Validate.isTrue(maxTravelDistance >= 0, "maxTravelDistance >= 0 (%f)", maxTravelDistance);

        this.location = location.clone();
        this.velocity = velocity.clone();
        this.maxTravelDistance = maxTravelDistance;
    }

    /**
     * 기준 위치에서 지정한 속도로 이동하는 반복 가능한 위치 인스턴스를 생성한다.
     *
     * @param location 기준 위치
     * @param velocity 속도
     */
    IterableLocation(@NonNull Location location, @NonNull Vector velocity) {
        this(location, velocity, Double.MAX_VALUE);
    }

    /**
     * 위치 반복자 인스턴스를 생성하여 반환한다.
     *
     * @return {@link LocationIterator}
     */
    @Override
    @NonNull
    public LocationIterator iterator() {
        return new LocationIterator(this);
    }


    /**
     * 이동하는 위치의 반복 기능을 제공하는 클래스.
     *
     * @see IterableLocation
     */
    public static final class LocationIterator implements Iterator<Location> {
        /** 반복 가능한 위치 인스턴스 */
        private final IterableLocation iterableLocation;
        /** 현재 위치 */
        private final Location location;
        /** 현재 속도 */
        private final Vector velocity;
        /** 이동한 거리. (단위: 블록) */
        private double travelDistance = 0;
        /** 반복 시작 여부 */
        private boolean isStarted = false;

        /**
         * 위치 반복자 인스턴스를 생성한다.
         *
         * @param iterableLocation 반복 가능한 위치
         */
        private LocationIterator(@NonNull IterableLocation iterableLocation) {
            this.iterableLocation = iterableLocation;
            this.location = iterableLocation.location.clone();
            this.velocity = iterableLocation.velocity.clone();
        }

        @Override
        public boolean hasNext() {
            return iterableLocation.maxTravelDistance == Double.MAX_VALUE || travelDistance + velocity.length() < iterableLocation.maxTravelDistance;
        }

        @Override
        public Location next() {
            if (!hasNext())
                throw new NoSuchElementException("다음 위치가 존재하지 않음");

            if (!isStarted) {
                isStarted = true;
                return location;
            }

            travelDistance += velocity.length();
            return location.add(velocity).clone();
        }
    }
}
