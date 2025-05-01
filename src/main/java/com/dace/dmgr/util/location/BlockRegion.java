package com.dace.dmgr.util.location;

import com.dace.dmgr.yaml.Serializer;
import com.dace.dmgr.yaml.SerializerUtil;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.util.NumberConversions;

import java.util.HashMap;
import java.util.Map;

/**
 * 특정 블록과 Y 좌표로 지정되는 지역을 나타내는 클래스.
 */
public final class BlockRegion implements Region {
    /** 기본값 */
    public static final BlockRegion DEFAULT = new BlockRegion(Material.STONE, 0);

    /** 확인할 블록의 종류 */
    private final Material material;
    /** 확인할 Y 좌표 */
    private final int yCoordinate;

    /**
     * 블록 지역 인스턴스를 생성한다.
     *
     * @param material    확인할 블록의 종류
     * @param yCoordinate 확인할 Y 좌표. 0~255 사이의 값
     */
    public BlockRegion(@NonNull Material material, int yCoordinate) {
        Validate.inclusiveBetween(0, 255, yCoordinate, "255 >= yCoordinate >= 0 (%d)", yCoordinate);

        this.material = material;
        this.yCoordinate = yCoordinate;
    }

    @Override
    public boolean isIn(@NonNull Entity entity) {
        Location loc = entity.getLocation();
        loc.setY(yCoordinate);

        return loc.getBlock().getType() == material;
    }

    /**
     * {@link BlockRegion}의 직렬화 처리기 클래스.
     */
    public static final class BlockRegionSerializer implements Serializer<BlockRegion, Map<String, Object>> {
        /** Material 직렬화 처리기 */
        private static final Serializer<Material, Object> SERIALIZER = SerializerUtil.getDefaultSerializer(Material.class);
        @Getter
        private static final BlockRegionSerializer instance = new BlockRegionSerializer();

        @Override
        @NonNull
        public Map<String, Object> serialize(@NonNull BlockRegion value) {
            Map<String, Object> map = new HashMap<>();
            map.put("block", SERIALIZER.serialize(value.material));
            map.put("y", value.yCoordinate);

            return map;
        }

        @Override
        @NonNull
        public BlockRegion deserialize(@NonNull Map<String, Object> value) {
            return new BlockRegion(SERIALIZER.deserialize(value.get("block")), NumberConversions.toInt(value.get("y")));
        }
    }
}
