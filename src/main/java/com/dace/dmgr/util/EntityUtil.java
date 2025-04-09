package com.dace.dmgr.util;

import com.dace.dmgr.DMGR;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.util.List;

/**
 * 엔티티 관련 기능을 제공하는 클래스.
 */
@UtilityClass
public final class EntityUtil {
    /**
     * 지정한 엔티티가 Citizens NPC인지 확인한다.
     *
     * @param entity 확인할 엔티티
     * @return NPC 여부
     */
    public static boolean isCitizensNPC(@NonNull Entity entity) {
        return entity.hasMetadata("NPC");
    }

    /**
     * 지정한 위치에 임시 갑옷 거치대 엔티티를 생성한다.
     *
     * @param spawnLocation 생성 위치
     * @return 갑옷 거치대 엔티티
     */
    @NonNull
    public static ArmorStand createTemporaryArmorStand(@NonNull Location spawnLocation) {
        ArmorStand armorStand = spawnLocation.getWorld().spawn(spawnLocation, ArmorStand.class);
        armorStand.setCustomName(DMGR.TEMPORARY_ENTITY_CUSTOM_NAME);
        armorStand.setSilent(true);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        armorStand.setAI(false);
        armorStand.setMarker(true);
        armorStand.setVisible(false);

        return armorStand;
    }

    /**
     * 엔티티를 지정한 위치로 순간이동시킨다.
     *
     * <p>기본 텔레포트는 엔티티의 탑승자({@link Entity#getPassengers()})가 있을 때 작동하지 않기 때문에 사용한다.</p>
     *
     * @param entity   대상 엔티티
     * @param location 이동할 위치
     */
    public static void teleport(@NonNull Entity entity, @NonNull Location location) {
        List<Entity> passengers = entity.getPassengers();

        passengers.forEach(passenger -> {
            entity.removePassenger(passenger);
            passenger.teleport(location);
        });

        entity.teleport(location);
        passengers.forEach(entity::addPassenger);
    }
}
