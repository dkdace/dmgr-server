package com.dace.dmgr;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * 월드가 지정되지 않은 전역 위치를 나타내는 클래스.
 *
 * <p>월드 복제 시 동일한 위치를 나타내기 위해 사용한다.</p>
 */
@AllArgsConstructor
@EqualsAndHashCode
public final class GlobalLocation {
    /** X 좌표 */
    private final double x;
    /** Y 좌표 */
    private final double y;
    /** Z 좌표 */
    private final double z;
    /** Yaw */
    private final float yaw;
    /** Pitch */
    private final float pitch;

    /**
     * 지정한 좌표로 전역 위치 인스턴스를 생성한다.
     *
     * @param x X 좌표
     * @param y Y 좌표
     * @param z Z 좌표
     */
    public GlobalLocation(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    /**
     * 현재 좌표를 바탕으로 지정한 월드의 위치를 반환한다.
     *
     * @param world 대상 월드
     * @return 위치
     */
    @NonNull
    public Location toLocation(@NonNull World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * 지정한 위치와 X, Y, Z 좌표가 같은지 확인한다.
     *
     * @param location 확인할 위치
     * @return 좌표가 같으면 {@code true} 반환
     */
    public boolean isSameLocation(@NonNull Location location) {
        return x == location.getX() && y == location.getY() && z == location.getZ();
    }
}
