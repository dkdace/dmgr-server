package com.dace.dmgr.util;

import com.dace.dmgr.combat.entity.Hitbox;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class LocationUtil {
    public static boolean isInCube(Location location, Location border1, Location border2) {
        return location.toVector().isInAABB(border1.toVector(), border2.toVector());
    }

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
                if (((Step) materialData).isInverted()) {
                    return location.getY() - Math.floor(location.getY()) < 0.5;
                } else {
                    return location.getY() - Math.floor(location.getY()) > 0.5;
                }
            } else if (materialData instanceof Stairs) {
                if (((Stairs) materialData).isInverted()) {
                    return location.getY() - Math.floor(location.getY()) < 0.5;
                } else {
                    return location.getY() - Math.floor(location.getY()) > 0.5;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean canPass(Location start, Location end) {
        for (Location loc : getLine(start, end)) {
            if (isNonSolid(loc)) return false;
        }
        return true;
    }

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

    public static Location setRelativeOffset(Location location, Vector direction, double offsetX, double offsetY, double offsetZ) {
        Location loc = location.clone();
        loc.setDirection(direction);

        loc.add(VectorUtil.getPitchAxis(loc).multiply(-offsetX));
        loc.add(VectorUtil.getYawAxis(loc).multiply(-offsetY));
        loc.add(VectorUtil.getRollAxis(loc).multiply(offsetZ));

        return loc;
    }

    public static Location setRelativeOffset(Location location, double offsetX, double offsetY, double offsetZ) {
        return setRelativeOffset(location, location.getDirection(), offsetX, offsetY, offsetZ);
    }

    public static boolean isInHitbox(Location location, Hitbox hitbox, float margin) {
        Location[] points = new Location[4];
        Location center = hitbox.getCenter();
        double sizeX = hitbox.getSizeX() + margin * 2;
        double sizeY = hitbox.getSizeY() + margin * 2;
        double sizeZ = hitbox.getSizeZ() + margin * 2;

        if (location.getY() < center.getY() - sizeY / 2 || location.getY() > center.getY() + sizeY / 2)
            return false;

        points[0] = LocationUtil.setRelativeOffset(center, -sizeX / 2, 0, sizeZ / 2);
        points[1] = LocationUtil.setRelativeOffset(center, -sizeX / 2, 0, -sizeZ / 2);
        points[2] = LocationUtil.setRelativeOffset(center, sizeX / 2, 0, -sizeZ / 2);
        points[3] = LocationUtil.setRelativeOffset(center, sizeX / 2, 0, sizeZ / 2);

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

    public static boolean isInHitbox(Location location, Hitbox hitbox) {
        return isInHitbox(location, hitbox, 0);
    }

    public static boolean isHeadshot(Location location, LivingEntity entity) {
        return false;
    }

}
