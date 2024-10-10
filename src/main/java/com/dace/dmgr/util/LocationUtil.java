package com.dace.dmgr.util;

import com.dace.dmgr.DMGR;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.material.*;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * 위치 관련 기능을 제공하는 클래스.
 */
@UtilityClass
public final class LocationUtil {
    /** 로비 스폰 위치 */
    private static final Location lobbyLocation = new Location(DMGR.getDefaultWorld(), 72.5, 64, 39.5, 90, 0);
    /** {@link LocationUtil#canPass(Location, Location)}에서 사용하는 위치 간 간격 */
    private static final double CAN_PASS_INTERVAL = 0.25;
    /** {@link LocationUtil#canPass(Location, Location)}의 최대 거리 */
    private static final int CAN_PASS_MAX_DISTANCE = 70;

    /**
     * 로비 스폰 위치를 반환한다.
     *
     * @return 스폰 위치
     */
    @NonNull
    public static Location getLobbyLocation() {
        return lobbyLocation.clone();
    }

    /**
     * 지정한 블록이 통과할 수 있는 블록인 지 확인한다.
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
     * 지정한 블록이 상호작용할 수 있는 블록인 지 확인한다.
     *
     * @param block 확인할 블록
     * @return 상호작용 가능하면 {@code true} 반환
     */
    public static boolean isInteractable(@NonNull Block block) {
        MaterialData materialData = block.getState().getData();
        if (materialData instanceof Openable || block.getState() instanceof Container)
            return true;

        switch (block.getType()) {
            case CAKE_BLOCK:
            case BEACON:
            case ANVIL:
            case ENDER_CHEST:
            case NOTE_BLOCK:
            case BED_BLOCK:
            case WOOD_BUTTON:
            case STONE_BUTTON:
            case LEVER:
            case DAYLIGHT_DETECTOR:
            case DAYLIGHT_DETECTOR_INVERTED:
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
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

        if (materialData instanceof Step) {
            if (((Step) materialData).isInverted())
                return location.getY() - Math.floor(location.getY()) < 0.5;
            else
                return location.getY() - Math.floor(location.getY()) > 0.5;
        } else if (materialData instanceof WoodenStep) {
            if (((WoodenStep) materialData).isInverted())
                return location.getY() - Math.floor(location.getY()) < 0.5;
            else
                return location.getY() - Math.floor(location.getY()) > 0.5;
        } else if (materialData instanceof Stairs) {
            if (((Stairs) materialData).isInverted())
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
     * @throws IllegalArgumentException 두 위치가 서로 다른 월드에 있으면 발생
     */
    @NonNull
    public static Vector getDirection(@NonNull Location start, @NonNull Location end) {
        validateLocation(start, end);

        return end.toVector().subtract(start.toVector()).normalize();
    }

    /**
     * 두 위치 사이를 통과할 수 있는지 확인한다.
     *
     * <p>최적화를 위해 {@link LocationUtil#CAN_PASS_MAX_DISTANCE}를 초과하는
     * 거리는 무조건 {@code false}를 반환한다.</p>
     *
     * <p>각종 스킬의 판정에 사용한다.</p>
     *
     * @param start 시작 위치
     * @param end   끝 위치
     * @return 통과 가능하면 {@code true} 반환
     * @throws IllegalArgumentException 두 위치가 서로 다른 월드에 있으면 발생
     */
    public static boolean canPass(@NonNull Location start, @NonNull Location end) {
        validateLocation(start, end);

        Vector direction = getDirection(start, end).multiply(CAN_PASS_INTERVAL);
        Location loc = start.clone();
        double distance = start.distance(end);
        if (distance > CAN_PASS_MAX_DISTANCE)
            return false;

        while (loc.distance(start) < distance) {
            if (!isNonSolid(loc.add(direction)))
                return false;
        }

        return true;
    }

    /**
     * 두 위치 사이에 있는 지정한 간격의 모든 위치를 반환한다.
     *
     * @param start    시작 위치
     * @param end      끝 위치
     * @param interval 위치 간 간격. 0을 초과하는 값
     * @return 해당 위치 목록
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 두 위치가 서로 다른 월드에 있으면 발생
     */
    @NonNull
    public static List<@NonNull Location> getLine(@NonNull Location start, @NonNull Location end, double interval) {
        validateLocation(start, end);
        if (interval <= 0)
            throw new IllegalArgumentException("'interval'이 0을 초과헤야 함");

        Vector direction = getDirection(start, end).multiply(interval);
        Location loc = start.clone();
        ArrayList<Location> locs = new ArrayList<>();
        double distance = start.distance(end);

        while (loc.distance(start) < distance) {
            locs.add(loc.clone());
            loc.add(direction);
        }

        return locs;
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
    public static Location getLocationFromOffset(@NonNull Location location, @NonNull Vector direction,
                                                 double offsetX, double offsetY, double offsetZ) {
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
     * Location loc = getLocationFromOffset(loc, dir, 2, 0, -1)
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
     * 지정한 엔티티가 특정 지역 안에 있는지 확인한다.
     *
     * @param entity     확인할 엔티티
     * @param regionName 지역 이름
     * @return {@code entity}가 {@code regionName} 내부에 있으면 {@code true} 반환
     */
    public static boolean isInRegion(@NonNull Entity entity, @NonNull String regionName) {
        RegionManager regionManager = WGBukkit.getRegionManager(entity.getWorld());

        for (ProtectedRegion region : regionManager.getApplicableRegions(entity.getLocation())) {
            if (region.getId().equalsIgnoreCase(regionName))
                return true;
        }

        return false;
    }

    /**
     * 지정한 위치의 특정 Y 좌표에 특정 블록이 있는 지 확인한다.
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
        if (yCoordinate < 0 || yCoordinate > 255)
            throw new IllegalArgumentException("'yCoordinate'가 0에서 255 사이여야 함");

        Location loc = location.clone();
        loc.setY(yCoordinate);
        return loc.getBlock().getType() == material;
    }

    /**
     * 두 위치가 서로 다른 월드에 있으면 예외를 발생시킨다.
     *
     * @param start 시작 위치
     * @param end   끝 위치
     */
    private static void validateLocation(@NonNull Location start, @NonNull Location end) {
        if (start.getWorld() != end.getWorld())
            throw new IllegalArgumentException("'start'와 'end'가 서로 다른 월드에 있음");
    }
}
