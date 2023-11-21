package com.dace.dmgr.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.*;
import org.bukkit.util.Vector;

/**
 * 위치 관련 기능을 제공하는 클래스.
 */
public final class LocationUtil {
    /**
     * 지정한 블록이 통과할 수 있는 블록인 지 확인한다.
     *
     * @param block 확인할 블록
     * @return 통과 가능하면 {@code true} 반환
     */
    private static boolean canPassBlock(Block block) {
        if (!block.getType().isSolid())
            return true;

        MaterialData materialData = block.getState().getData();
        if (materialData instanceof Step && block.getType().isOccluding())
            return false;
        if (materialData instanceof Step || materialData instanceof Stairs || materialData instanceof Gate ||
                materialData instanceof Door || materialData instanceof TrapDoor)
            return true;

        switch (block.getType()) {
            case THIN_GLASS:
            case STAINED_GLASS_PANE:
            case FENCE:
            case SPRUCE_FENCE:
            case BIRCH_FENCE:
            case JUNGLE_FENCE:
            case ACACIA_FENCE_GATE:
            case DARK_OAK_FENCE:
            case IRON_FENCE:
            case COBBLE_WALL:
            case SIGN_POST:
            case WALL_SIGN:
                return true;
            default:
                return false;
        }
    }

    /**
     * 지정한 위치를 통과할 수 있는지 확인한다.
     *
     * <p>통과 가능한 블록은 {@link LocationUtil#canPassBlock(Block)}에서 판단한다.</p>
     *
     * <p>각종 스킬의 판정에 사용한다.</p>
     *
     * @param location 확인할 위치
     * @return 통과 가능하면 {@code true} 반환
     */
    public static boolean isNonSolid(Location location) {
        Block block = location.getBlock();

        if (canPassBlock(block)) {
            MaterialData materialData = block.getState().getData();

            if (materialData instanceof Step) {
                if (((Step) materialData).isInverted())
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

        return false;
    }

    /**
     * 두 위치 사이를 통과할 수 있는지 확인한다.
     *
     * <p>각종 스킬의 판정에 사용한다.</p>
     *
     * @param start 시작 위치
     * @param end   끝 위치
     * @return 통과 가능하면 {@code true} 반환
     */
    public static boolean canPass(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector()).normalize().multiply(0.25);
        Location loc = start.clone();
        double distance = start.distance(end);

        while (loc.distance(start) < distance) {
            if (!isNonSolid(loc.add(direction)))
                return false;
        }

        return true;
    }

    /**
     * 지정한 위치와 방향을 기준으로 오프셋이 적용된 최종 위치를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // loc과 dir을 기준으로 2m 오른쪽, 1m 뒤쪽의 위치 반환
     * Location loc = getLocationFromOffset(loc, dir, 2, 0, -1)
     * }</pre>
     *
     * @param location  기준 위치
     * @param direction 기준 방향
     * @param offsetX   왼쪽(-) / 오른쪽(+)
     * @param offsetY   아래(-) / 위(+)
     * @param offsetZ   뒤(-) / 앞(+)
     * @return 최종 위치
     */
    public static Location getLocationFromOffset(Location location, Vector direction, double offsetX, double offsetY, double offsetZ) {
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
     * <pre>{@code
     * // loc의 방향을 기준으로 2m 오른쪽, 1m 뒤쪽의 위치 반환
     * Location loc = getLocationFromOffset(loc, dir, 2, 0, -1)
     * }</pre>
     *
     * @param location 기준 위치
     * @param offsetX  왼쪽(-) / 오른쪽(+)
     * @param offsetY  아래(-) / 위(+)
     * @param offsetZ  뒤(-) / 앞(+)
     * @return 최종 위치
     */
    public static Location getLocationFromOffset(Location location, double offsetX, double offsetY, double offsetZ) {
        return getLocationFromOffset(location, location.getDirection(), offsetX, offsetY, offsetZ);
    }

    /**
     * 지정한 위치의 특정 Y 좌표에 특정 블록이 있는 지 확인한다.
     *
     * <p>주로 간단하게 지역을 확인할 때 사용한다.</p>
     *
     * @param location    확인할 위치
     * @param yCoordinate Y 좌표
     * @param material    블록의 종류
     * @return {@code material}에 해당하는 블록이 {@code location}의 Y 좌표 {@code yCoordinate}에 있으면 {@code true} 반환
     */
    public static boolean isInSameBlockXZ(Location location, int yCoordinate, Material material) {
        Location loc = location.clone();
        loc.setY(yCoordinate);
        return loc.getBlock().getType() == material;
    }
}
