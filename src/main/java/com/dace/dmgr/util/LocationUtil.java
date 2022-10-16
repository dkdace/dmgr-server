package com.dace.dmgr.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
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

    public static Location setRelativeOffset(Location location, double offsetX, double offsetY, double offsetZ) {
        Location loc = location.clone();

        loc.add(VectorUtil.getPitchAxis(loc).multiply(-offsetX));
        loc.add(VectorUtil.getYawAxis(loc).multiply(offsetX));
        loc.add(VectorUtil.getRollAxis(loc).multiply(offsetX));

        return loc;
    }
}
