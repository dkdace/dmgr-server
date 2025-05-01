package com.dace.dmgr.util.location;

import com.dace.dmgr.util.VectorUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.material.*;
import org.bukkit.util.Vector;

/**
 * 위치 및 블록 관련 기능을 제공하는 클래스.
 */
@UtilityClass
public final class LocationUtil {
    /** {@link LocationUtil#canPass(Location, Location)}에서 사용하는 위치 간격 */
    private static final double CAN_PASS_INTERVAL = 1 / 4.0;
    /** {@link LocationUtil#canPass(Location, Location)}의 최대 거리 */
    private static final int CAN_PASS_MAX_DISTANCE = 70;
    /** {@link LocationUtil#getNearestAgainstEdge(Location, Vector)}에서 사용하는 위치 간격 */
    private static final double GET_EDGE_INTERVAL = 1 / 8.0;

    /**
     * 지정한 블록이 통과할 수 있는 블록인지 확인한다.
     *
     * @param block 확인할 블록
     * @return 통과 가능하면 {@code true} 반환
     */
    private static boolean isPassable(@NonNull Block block) {
        if (!block.getType().isSolid())
            return true;

        MaterialData materialData = block.getState().getData();
        if ((materialData instanceof Step || materialData instanceof WoodenStep) && block.getType().isOccluding())
            return false;
        if (materialData instanceof Step || materialData instanceof WoodenStep || materialData instanceof Stairs || materialData instanceof Openable)
            return true;

        switch (block.getType()) {
            case THIN_GLASS:
            case STAINED_GLASS_PANE:
            case FENCE:
            case SPRUCE_FENCE:
            case BIRCH_FENCE:
            case JUNGLE_FENCE:
            case ACACIA_FENCE:
            case DARK_OAK_FENCE:
            case NETHER_FENCE:
            case IRON_FENCE:
            case COBBLE_WALL:
            case SIGN_POST:
            case WALL_SIGN:
            case BANNER:
            case WALL_BANNER:
            case STANDING_BANNER:
            case WOOD_PLATE:
            case STONE_PLATE:
            case IRON_PLATE:
            case GOLD_PLATE:
                return true;
            default:
                return false;
        }
    }

    /**
     * 지정한 위치를 통과할 수 있는지 확인한다.
     *
     * <p>통과 가능한 블록은 {@link LocationUtil#isPassable(Block)}에서 판단한다.</p>
     *
     * <p>각종 스킬의 판정에 사용한다.</p>
     *
     * @param location 확인할 위치
     * @return 통과 가능하면 {@code true} 반환
     */
    public static boolean isNonSolid(@NonNull Location location) {
        Block block = location.getBlock();
        if (!isPassable(block))
            return false;

        MaterialData materialData = block.getState().getData();
        if (materialData instanceof Step || materialData instanceof WoodenStep || materialData instanceof Stairs) {
            if (materialData instanceof Step && ((Step) materialData).isInverted()
                    || materialData instanceof WoodenStep && ((WoodenStep) materialData).isInverted()
                    || materialData instanceof Stairs && ((Stairs) materialData).isInverted())
                return location.getY() - Math.floor(location.getY()) < 0.5;
            else
                return location.getY() - Math.floor(location.getY()) > 0.5;
        }

        return true;
    }

    /**
     * 시작 위치에서 끝 위치로 향하는 방향을 반환한다.
     *
     * @param start 시작 위치
     * @param end   끝 위치
     * @return 방향
     * @throws IllegalArgumentException {@code start}와 {@code end}가 서로 다른 월드에 있으면 발생
     */
    @NonNull
    public static Vector getDirection(@NonNull Location start, @NonNull Location end) {
        Validate.isTrue(start.getWorld() == end.getWorld(), "start == end (false)");

        return end.toVector().subtract(start.toVector()).normalize();
    }

    /**
     * 두 위치 사이를 통과할 수 있는지 확인한다.
     *
     * <p>최적화를 위해 {@link LocationUtil#CAN_PASS_MAX_DISTANCE}를 초과하는 거리는 무조건 {@code false}를 반환한다.</p>
     *
     * <p>각종 스킬의 판정에 사용한다.</p>
     *
     * @param start 시작 위치
     * @param end   끝 위치
     * @return 통과 가능하면 {@code true} 반환
     * @throws IllegalArgumentException {@code start}와 {@code end}가 서로 다른 월드에 있으면 발생
     */
    public static boolean canPass(@NonNull Location start, @NonNull Location end) {
        Validate.isTrue(start.getWorld() == end.getWorld(), "start == end (false)");

        if (start.distance(end) > CAN_PASS_MAX_DISTANCE)
            return false;

        for (Location loc : getLine(start, end, CAN_PASS_INTERVAL))
            if (!isNonSolid(loc))
                return false;

        return true;
    }

    /**
     * 시작 위치에서 끝 위치로 향하는 반복 가능한 위치를 반환한다.
     *
     * @param start    시작 위치
     * @param end      끝 위치
     * @param interval 위치 간격. (단위: 블록). 0 이상의 값
     * @return 반복 가능한 위치
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 {@code start}와 {@code end}가 서로 다른 월드에 있으면 발생
     */
    @NonNull
    public static IterableLocation getLine(@NonNull Location start, @NonNull Location end, double interval) {
        return new IterableLocation(start, getDirection(start, end).multiply(interval), start.distance(end));
    }

    /**
     * 기준 위치에서 지정한 속도로 이동하는 반복 가능한 위치를 반환한다.
     *
     * @param location    기준 위치
     * @param velocity    속도
     * @param maxDistance 최대 이동 거리. (단위: 블록). 0 이상의 값
     * @return 반복 가능한 위치
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static IterableLocation getIterable(@NonNull Location location, @NonNull Vector velocity, double maxDistance) {
        return new IterableLocation(location, velocity, maxDistance);
    }

    /**
     * 기준 위치에서 지정한 속도로 이동하는 반복 가능한 위치를 반환한다.
     *
     * @param location 기준 위치
     * @param velocity 속도
     * @return 반복 가능한 위치
     */
    @NonNull
    public static IterableLocation getIterable(@NonNull Location location, @NonNull Vector velocity) {
        return new IterableLocation(location, velocity);
    }

    /**
     * 기준 위치에서 지정한 방향으로 이동했을 때 통과 불가능한 블록의 가장자리와 가장 가까운 위치를 반환한다.
     *
     * @param location    기준 위치
     * @param direction   방향
     * @param maxDistance 최대 탐지 거리. (단위: 블록). 0 이상의 값
     * @return 가장자리와 가장 가까운 위치
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static Location getNearestAgainstEdge(@NonNull Location location, @NonNull Vector direction, double maxDistance) {
        Location prevLoc = location.clone();
        boolean isNonSolid = isNonSolid(prevLoc);

        for (Location loc : getIterable(location, direction.clone().normalize().multiply(GET_EDGE_INTERVAL), maxDistance)) {
            if (isNonSolid != isNonSolid(loc))
                return isNonSolid ? prevLoc : loc;

            prevLoc = loc;
        }

        return prevLoc;
    }

    /**
     * 기준 위치에서 지정한 방향으로 이동했을 때 통과 불가능한 블록의 가장자리와 가장 가까운 위치를 반환한다.
     *
     * <p>최대 탐지 거리는 {@link LocationUtil#CAN_PASS_MAX_DISTANCE}이다.</p>
     *
     * @param location  기준 위치
     * @param direction 방향
     * @return 가장자리와 가장 가까운 위치
     */
    @NonNull
    public static Location getNearestAgainstEdge(@NonNull Location location, @NonNull Vector direction) {
        return getNearestAgainstEdge(location, direction, CAN_PASS_MAX_DISTANCE);
    }

    /**
     * 지정한 위치와 방향을 기준으로 오프셋이 적용된 최종 위치를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // loc과 dir을 기준으로 2m 오른쪽, 1m 뒤쪽의 위치 반환
     * Location loc = getLocationFromOffset(loc, dir, 2, 0, -1)
     * </code></pre>
     *
     * @param location  기준 위치
     * @param direction 기준 방향
     * @param offsetX   왼쪽(-) / 오른쪽(+). (단위: 블록)
     * @param offsetY   아래(-) / 위(+). (단위: 블록)
     * @param offsetZ   뒤(-) / 앞(+). (단위: 블록)
     * @return 최종 위치
     */
    @NonNull
    public static Location getLocationFromOffset(@NonNull Location location, @NonNull Vector direction, double offsetX, double offsetY, double offsetZ) {
        Location loc = location.clone();
        loc.setDirection(direction);

        loc.add(VectorUtil.getPitchAxis(loc).multiply(-offsetX));
        loc.add(VectorUtil.getYawAxis(loc).multiply(-offsetY));
        loc.add(VectorUtil.getRollAxis(loc).multiply(offsetZ));

        return loc;
    }

    /**
     * 지정한 위치를 기준으로 오프셋이 적용된 최종 위치를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // loc의 방향을 기준으로 2m 오른쪽, 1m 뒤쪽의 위치 반환
     * Location loc = getLocationFromOffset(loc, 2, 0, -1)
     * </code></pre>
     *
     * @param location 기준 위치
     * @param offsetX  왼쪽(-) / 오른쪽(+). (단위: 블록)
     * @param offsetY  아래(-) / 위(+). (단위: 블록)
     * @param offsetZ  뒤(-) / 앞(+). (단위: 블록)
     * @return 최종 위치
     */
    @NonNull
    public static Location getLocationFromOffset(@NonNull Location location, double offsetX, double offsetY, double offsetZ) {
        return getLocationFromOffset(location, location.getDirection(), offsetX, offsetY, offsetZ);
    }

    /**
     * 지정한 엔티티가 특정 WorldGuard 지역 안에 있는지 확인한다.
     *
     * @param entity     확인할 엔티티
     * @param regionName 지역 이름
     * @return {@code entity}가 {@code regionName} 내부에 있으면 {@code true} 반환
     */
    public static boolean isInRegion(@NonNull Entity entity, @NonNull String regionName) {
        RegionManager regionManager = WGBukkit.getRegionManager(entity.getWorld());

        for (ProtectedRegion region : regionManager.getApplicableRegions(entity.getLocation()))
            if (region.getId().equalsIgnoreCase(regionName))
                return true;

        return false;
    }

    /**
     * 지정한 위치의 특정 Y 좌표에 특정 블록이 있는지 확인한다.
     *
     * <p>주로 간단하게 지역을 확인할 때 사용한다.</p>
     *
     * @param location    확인할 위치
     * @param yCoordinate Y 좌표. 0~255 사이의 값
     * @param material    블록의 종류
     * @return {@code material}에 해당하는 블록이 {@code location}의 Y 좌표 {@code yCoordinate}에 있으면 {@code true} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static boolean isInSameBlockXZ(@NonNull Location location, int yCoordinate, @NonNull Material material) {
        Validate.inclusiveBetween(0, 255, yCoordinate, "255 >= yCoordinate >= 0 (%d)", yCoordinate);

        Location loc = location.clone();
        loc.setY(yCoordinate);

        return loc.getBlock().getType() == material;
    }
}
