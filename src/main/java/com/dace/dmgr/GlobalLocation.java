package com.dace.dmgr;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.NumberConversions;

import java.util.HashMap;
import java.util.Map;

/**
 * 월드가 지정되지 않은 전역 위치를 나타내는 클래스.
 *
 * <p>월드 복제 시 동일한 위치를 나타내기 위해 사용한다.</p>
 *
 * <p>직렬화 형식:</p>
 *
 * <table>
 * <tr><th>키</th><th>값</th><th>예시</th></tr>
 * <tr><td>x</td><td>{@link GlobalLocation#x}</td><td>1.0</td>
 * <tr><td>y</td><td>{@link GlobalLocation#y}</td><td>1.0</td>
 * <tr><td>z</td><td>{@link GlobalLocation#z}</td><td>1.0</td>
 * <tr><td>yaw</td><td>{@link GlobalLocation#yaw}</td><td>90.0</td>
 * <tr><td>pitch</td><td>{@link GlobalLocation#pitch}</td><td>0.0</td>
 * </table>
 */
@AllArgsConstructor
@EqualsAndHashCode
@SerializableAs("GlobalLocation")
public final class GlobalLocation implements ConfigurationSerializable {
    /** 모든 값이 0인 전역 위치 */
    public static final GlobalLocation ZERO = new GlobalLocation(0, 0, 0, 0, 0);

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

    @NonNull
    @SuppressWarnings("unused")
    public static GlobalLocation deserialize(@NonNull Map<String, Object> map) {
        return new GlobalLocation(
                NumberConversions.toDouble(map.get("x")),
                NumberConversions.toDouble(map.get("y")),
                NumberConversions.toDouble(map.get("z")),
                NumberConversions.toFloat(map.get("yaw")),
                NumberConversions.toFloat(map.get("pitch")));
    }

    @Override
    @NonNull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("yaw", yaw);
        map.put("pitch", pitch);

        return map;
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
}
