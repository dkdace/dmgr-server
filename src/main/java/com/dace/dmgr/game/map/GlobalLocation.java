package com.dace.dmgr.game.map;

import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * 월드가 지정되지 않은 전역 위치를 나타내는 클래스.
 *
 * <p>월드 복제 시 동일한 위치를 나타내기 위해 사용한다.</p>
 */
@AllArgsConstructor
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
     * 현재 좌표를 바탕으로 지정한 월드의 위치를 반환한다.
     *
     * @param world 대상 월드
     * @return 위치
     */
    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }
}
