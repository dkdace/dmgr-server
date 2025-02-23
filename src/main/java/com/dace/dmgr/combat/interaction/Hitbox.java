package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.*;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * 공격 판정에 사용하는 히트박스 클래스.
 */
public final class Hitbox {
    /** (1, 0, 0) 값의 벡터 인스턴스 */
    private static final Vector X_VECTOR = new Vector(1, 0, 0);
    /** (0, 1, 0) 값의 벡터 인스턴스 */
    private static final Vector Y_VECTOR = new Vector(0, 1, 0);

    /** 가로. (단위: 블록) */
    private final double sizeX;
    /** 높이. (단위: 블록) */
    @Getter
    private final double sizeY;
    /** 세로. (단위: 블록) */
    private final double sizeZ;
    /** 히트박스의 Pitch 고정 여부 */
    private final boolean isPitchFixed;
    /** 가로/세로 크기 배수 */
    @Getter
    @Setter
    private double sizeMultiplier = 1;
    /** 중앙 위치 오프셋. 왼쪽(-) / 오른쪽(+). (단위 : 블록) */
    @Getter
    @Setter
    private double offsetX;
    /** 중앙 위치 오프셋. 아래(-) / 위(+). (단위 : 블록) */
    @Getter
    @Setter
    private double offsetY;
    /** 중앙 위치 오프셋. 뒤(-) / 앞(+). (단위 : 블록) */
    @Getter
    @Setter
    private double offsetZ;
    /** 축 기준 중앙 위치 오프셋. -X / +X. (단위 : 블록) */
    @Getter
    @Setter
    private double axisOffsetX;
    /** 축 기준 중앙 위치 오프셋. -Y / +Y. (단위 : 블록) */
    @Getter
    @Setter
    private double axisOffsetY;
    /** 축 기준 중앙 위치 오프셋. -Z / +Z. (단위 : 블록) */
    @Getter
    @Setter
    private double axisOffsetZ;
    /** 중앙 위치 */
    private Location center;

    private Hitbox(Builder builder) {
        this.sizeX = builder.sizeX;
        this.sizeY = builder.sizeY;
        this.sizeZ = builder.sizeZ;
        this.offsetX = builder.offsetX;
        this.offsetY = builder.offsetY;
        this.offsetZ = builder.offsetZ;
        this.axisOffsetX = builder.axisOffsetX;
        this.axisOffsetY = builder.axisOffsetY;
        this.axisOffsetZ = builder.axisOffsetZ;
        this.isPitchFixed = builder.isPitchFixed;

        setBaseLocation(builder.location);
    }

    /**
     * 빌더 인스턴스를 생성하여 반환한다.
     *
     * @param location 시작 위치
     * @param sizeX    가로. (단위: 블록). 0을 초과하는 값
     * @param sizeY    높이. (단위: 블록). 0을 초과하는 값
     * @param sizeZ    세로. (단위: 블록). 0을 초과하는 값
     * @return {@link Builder}
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static Builder builder(@NonNull Location location, double sizeX, double sizeY, double sizeZ) {
        Validate.isTrue(sizeX > 0, "sizeX > 0 (%f)", sizeX);
        Validate.isTrue(sizeY > 0, "sizeY > 0 (%f)", sizeY);
        Validate.isTrue(sizeZ > 0, "sizeZ > 0 (%f)", sizeZ);

        return new Builder(location, sizeX, sizeY, sizeZ);
    }

    /**
     * 히트박스의 중앙 위치를 반환한다.
     *
     * @return 중앙 위치
     */
    @NonNull
    public Location getCenter() {
        return center.clone();
    }

    /**
     * 히트박스 위치의 기준점을 설정한다.
     *
     * @param location 기준 위치
     */
    public void setBaseLocation(@NonNull Location location) {
        Location loc = location.clone();
        if (isPitchFixed)
            loc.setPitch(0);

        center = LocationUtil.getLocationFromOffset(loc, offsetX, offsetY, offsetZ)
                .add(axisOffsetX, axisOffsetY, axisOffsetZ);
    }

    /**
     * @return 가로. (단위: 블록)
     */
    public double getSizeX() {
        return sizeX * sizeMultiplier;
    }

    /**
     * @return 세로. (단위: 블록)
     */
    public double getSizeZ() {
        return sizeZ * sizeMultiplier;
    }

    /**
     * 히트박스 안에서 지정한 위치까지 가장 가까운 위치를 반환한다.
     *
     * @param location 확인할 위치
     * @return 가장 가까운 위치
     */
    @NonNull
    public Location getNearestLocation(@NonNull Location location) {
        Vector rotVec = VectorUtil.getRotatedVector(VectorUtil.getRotatedVector(location.clone().subtract(center).toVector(),
                Y_VECTOR, center.getYaw()), X_VECTOR, -center.getPitch());
        Location rotLoc = center.clone().add(rotVec);
        Location cuboidEdge = center.clone().add(
                (rotLoc.getX() > center.getX() ? 1 : -1) * Math.min(getSizeX() / 2, Math.abs(rotLoc.getX() - center.getX())),
                (rotLoc.getY() > center.getY() ? 1 : -1) * Math.min(getSizeY() / 2, Math.abs(rotLoc.getY() - center.getY())),
                (rotLoc.getZ() > center.getZ() ? 1 : -1) * Math.min(getSizeZ() / 2, Math.abs(rotLoc.getZ() - center.getZ())));

        Vector retVec = VectorUtil.getRotatedVector(VectorUtil.getRotatedVector(cuboidEdge.clone().subtract(center).toVector(),
                X_VECTOR, center.getPitch()), Y_VECTOR, -center.getYaw());

        return center.clone().add(retVec);
    }

    /**
     * 지정한 위치까지의 거리를 반환한다.
     *
     * @param location 확인할 위치
     * @return 지정한 위치까지의 거리. (단위: 블록)
     */
    public double getDistance(@NonNull Location location) {
        return getNearestLocation(location).distance(location);
    }

    /**
     * 지정한 위치의 구체가 히트박스와 접하고 있는지 확인한다.
     *
     * @param location 확인할 위치
     * @param radius   판정 구체의 반지름. (단위: 블록). 0 이상의 값
     * @return 판정 구체가 히트박스와 접하고 있으면 {@code true} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public boolean isInHitbox(@NonNull Location location, double radius) {
        Validate.isTrue(radius >= 0, "radius >= 0 (%f)", radius);
        return getDistance(location) <= radius;
    }

    /**
     * {@link Hitbox}의 빌더 클래스.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {
        private final Location location;
        private final double sizeX;
        private final double sizeY;
        private final double sizeZ;
        private double offsetX = 0;
        private double offsetY = 0;
        private double offsetZ = 0;
        private double axisOffsetX = 0;
        private double axisOffsetY = 0;
        private double axisOffsetZ = 0;
        private boolean isPitchFixed = false;

        /**
         * 히트박스의 중앙 위치 오프셋을 설정한다.
         *
         * @param offsetX 중앙 위치 오프셋. 왼쪽(-) / 오른쪽(+). (단위 : 블록)
         * @param offsetY 중앙 위치 오프셋. 아래(-) / 위(+). (단위 : 블록)
         * @param offsetZ 중앙 위치 오프셋. 뒤(-) / 앞(+). (단위 : 블록)
         * @return {@link Builder}
         * @see Builder#offsetY(double)
         */
        @NonNull
        public Builder offset(double offsetX, double offsetY, double offsetZ) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;

            return this;
        }

        /**
         * 히트박스의 Y축 중앙 위치 오프셋을 설정한다.
         *
         * @param offsetY 중앙 위치 오프셋. 아래(-) / 위(+). (단위 : 블록)
         * @return {@link Builder}
         */
        @NonNull
        public Builder offsetY(double offsetY) {
            this.offsetY = offsetY;
            return this;
        }

        /**
         * 히트박스의 축 기준 중앙 위치 오프셋을 설정한다.
         *
         * @param axisOffsetX 축 기준 중앙 위치 오프셋. -X / +X. (단위 : 블록)
         * @param axisOffsetY 축 기준 중앙 위치 오프셋. -Y / +Y. (단위 : 블록)
         * @param axisOffsetZ 축 기준 중앙 위치 오프셋. -Z / +Z. (단위 : 블록)
         * @return {@link Builder}
         * @see Builder#axisOffsetY(double)
         */
        @NonNull
        public Builder axisOffset(double axisOffsetX, double axisOffsetY, double axisOffsetZ) {
            this.axisOffsetX = axisOffsetX;
            this.axisOffsetY = axisOffsetY;
            this.axisOffsetZ = axisOffsetZ;

            return this;
        }

        /**
         * 히트박스의 Y축 기준 중앙 위치 오프셋을 설정한다.
         *
         * @param axisOffsetY 축 기준 중앙 위치 오프셋. -Y / +Y. (단위 : 블록)
         * @return {@link Builder}
         */
        @NonNull
        public Builder axisOffsetY(double axisOffsetY) {
            this.axisOffsetY = axisOffsetY;
            return this;
        }

        /**
         * 히트박스의 회전이 Pitch 축의 영향을 받지 않도록 설정한다.
         *
         * @return {@link Builder}
         */
        @NonNull
        public Builder pitchFixed() {
            this.isPitchFixed = true;
            return this;
        }

        /**
         * 히트박스 인스턴스를 생성하여 반환한다.
         *
         * @return {@link Hitbox}
         */
        @NonNull
        public Hitbox build() {
            return new Hitbox(this);
        }
    }
}
