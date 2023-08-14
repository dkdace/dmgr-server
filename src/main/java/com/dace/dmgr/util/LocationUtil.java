package com.dace.dmgr.util;

import com.dace.dmgr.combat.entity.Hitbox;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * 위치 관련 기능을 제공하는 클래스.
 */
public final class LocationUtil {
    /**
     * 지정한 위치를 통과할 수 있는지 확인한다.
     *
     * <p>통과 가능한 블록이란 유리판, 울타리, 물 등을 말한다.</p>
     *
     * <p>각종 스킬의 판정에 사용한다.</p>
     *
     * @param location 확인할 위치
     * @return 통과 가능하면 {@code true} 반환
     */
    public static boolean isNonSolid(Location location) {
        Block block = location.getBlock();

        if (block.isEmpty())
            return true;

        if (!block.getType().isOccluding()) {
            switch (block.getType()) {
                case GLASS:
                case STAINED_GLASS:
                case GLOWSTONE:
                case BEACON:
                case SEA_LANTERN:
                case CAULDRON:
                case ANVIL:
                    return false;
            }

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
        for (Location loc : getLine(start, end)) {
            if (isNonSolid(loc)) return false;
        }
        return true;
    }

    /**
     * 두 위치 사이에 있는 1m 간격의 모든 위치를 반환한다.
     *
     * @param start 시작 위치
     * @param end   끝 위치
     * @return 해당 위치 목록
     */
    public static List<Location> getLine(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector());
        Location loc = start.clone();
        List<Location> locList = new ArrayList<>();

        while (loc.distance(start) < start.distance(end)) {
            loc.add(direction);
            locList.add(loc);
        }

        return locList;
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
     * 지정한 위치가 특정 히트박스의 내부에 있는 지 확인한다.
     *
     * @param location 확인할 위치
     * @param hitbox   히트박스
     * @param margin   마진. 히트박스 크기에 margin을 더해 계산
     * @return {@code location}이 {@code hitbox}의 내부에 있으면 {@code true} 반환
     * @see Hitbox
     */
    public static boolean isInHitbox(Location location, Hitbox hitbox, float margin) {
        Location[] points = new Location[4];
        Location center = hitbox.getCenter();
        double sizeX = hitbox.getSizeX() + margin * 2;
        double sizeY = hitbox.getSizeY() + margin * 2;
        double sizeZ = hitbox.getSizeZ() + margin * 2;

        if (location.getY() < center.getY() - sizeY / 2 || location.getY() > center.getY() + sizeY / 2)
            return false;

        points[0] = LocationUtil.getLocationFromOffset(center, -sizeX / 2, 0, sizeZ / 2);
        points[1] = LocationUtil.getLocationFromOffset(center, -sizeX / 2, 0, -sizeZ / 2);
        points[2] = LocationUtil.getLocationFromOffset(center, sizeX / 2, 0, -sizeZ / 2);
        points[3] = LocationUtil.getLocationFromOffset(center, sizeX / 2, 0, sizeZ / 2);

        boolean inside = false;

        for (int i = 0, j = 3; i < 4; j = i++) {
            if (((points[i].getZ() > location.getZ()) != (points[j].getZ() > location.getZ())) &&
                    (location.getX() < (points[j].getX() - points[i].getX()) *
                            (location.getZ() - points[i].getZ()) / (points[j].getZ() - points[i].getZ()) + points[i].getX())) {
                inside = !inside;
            }
        }

        return inside;
    }

    /**
     * 지정한 위치가 특정 히트박스의 내부에 있는 지 확인한다.
     *
     * @param location 확인할 위치
     * @param hitbox   히트박스
     * @return {@code location}이 {@code hitbox}의 내부에 있으면 {@code true} 반환
     * @see Hitbox
     */
    public static boolean isInHitbox(Location location, Hitbox hitbox) {
        return isInHitbox(location, hitbox, 0);
    }
}
