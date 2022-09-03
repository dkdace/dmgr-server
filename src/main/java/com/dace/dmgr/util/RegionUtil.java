package com.dace.dmgr.util;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Entity;

public class RegionUtil {
    public static boolean isInRegion(Entity entity, String... regionName) {
        RegionManager regionManager = WGBukkit.getRegionManager(entity.getWorld());

        for (ProtectedRegion region : regionManager.getApplicableRegions(entity.getLocation())) {
            for (String s : regionName) {
                if (!region.getId().equalsIgnoreCase(s)) return false;
            }
            return true;
        }

        return false;
    }
}
