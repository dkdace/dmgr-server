package com.dace.dmgr.util.location;

import com.dace.dmgr.yaml.Serializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 두 위치를 통해 지정되는 직육면체 지역을 나타내는 클래스.
 */
@AllArgsConstructor
public final class CuboidRegion implements Region {
    /** 기본값 */
    public static final CuboidRegion DEFAULT = new CuboidRegion(GlobalLocation.ZERO, GlobalLocation.ZERO);

    /** 첫 번째 위치 */
    @NonNull
    private final GlobalLocation pos1;
    /** 두 번째 위치 */
    @NonNull
    private final GlobalLocation pos2;

    @Override
    public boolean isIn(@NonNull Entity entity) {
        Location location = entity.getLocation();

        return location.getX() >= pos1.getX() && location.getX() <= pos2.getX()
                && location.getY() >= pos1.getY() && location.getY() <= pos2.getY()
                && location.getZ() >= pos1.getZ() && location.getZ() <= pos2.getZ();
    }

    /**
     * {@link CuboidRegion}의 직렬화 처리기 클래스.
     */
    public static final class CuboidRegionSerializer implements Serializer<CuboidRegion, List<Map<String, Number>>> {
        /** GlobalLocation 직렬화 처리기 */
        private static final GlobalLocation.GlobalLocationSerializer SERIALIZER = GlobalLocation.GlobalLocationSerializer.getInstance();
        @Getter
        private static final CuboidRegionSerializer instance = new CuboidRegionSerializer();

        @Override
        @NonNull
        public List<Map<String, Number>> serialize(@NonNull CuboidRegion value) {
            return Arrays.asList(SERIALIZER.serialize(value.pos1), SERIALIZER.serialize(value.pos2));
        }

        @Override
        @NonNull
        public CuboidRegion deserialize(@NonNull List<Map<String, Number>> value) {
            return new CuboidRegion(SERIALIZER.deserialize(value.get(0)), SERIALIZER.deserialize(value.get(1)));
        }
    }
}
