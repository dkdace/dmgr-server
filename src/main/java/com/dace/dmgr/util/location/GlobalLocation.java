package com.dace.dmgr.util.location;

import com.dace.dmgr.yaml.Serializer;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;

import java.util.HashMap;
import java.util.Map;

/**
 * 월드가 지정되지 않은 전역 위치를 나타내는 클래스.
 *
 * <p>월드 복제 시 동일한 위치를 나타내기 위해 사용한다.</p>
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public final class GlobalLocation {
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
     * {@link GlobalLocation}의 직렬화 처리기 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class GlobalLocationSerializer implements Serializer<GlobalLocation, Map<String, Number>> {
        @Getter
        private static final GlobalLocationSerializer instance = new GlobalLocationSerializer();

        @Override
        @NonNull
        public Map<String, Number> serialize(@NonNull GlobalLocation value) {
            Map<String, Number> map = new HashMap<>();
            map.put("x", value.x);
            map.put("y", value.y);
            map.put("z", value.z);
            map.put("yaw", value.yaw);
            map.put("pitch", value.pitch);

            return map;
        }

        @Override
        @NonNull
        public GlobalLocation deserialize(@NonNull Map<String, Number> value) {
            return new GlobalLocation(
                    NumberConversions.toDouble(value.get("x")),
                    NumberConversions.toDouble(value.get("y")),
                    NumberConversions.toDouble(value.get("z")),
                    NumberConversions.toFloat(value.get("yaw")),
                    NumberConversions.toFloat(value.get("pitch")));
        }
    }
}
