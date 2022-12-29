package com.dace.dmgr.util;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Entity;

/**
 * 지역(Region) 관련 기능을 제공하는 클래스.
 */
public class RegionUtil {
    /**
     * 지정한 엔티티가 특정 지역 안에 있는지 확인한다.
     *
     * @param entity     확인할 엔티티
     * @param regionName 지역 이름
     * @return {@code entity}가 {@code regionName} 내부에 있으면 {@code true} 반환
     */
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
