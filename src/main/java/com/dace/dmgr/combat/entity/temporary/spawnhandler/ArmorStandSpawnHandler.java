package com.dace.dmgr.combat.entity.temporary.spawnhandler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

/**
 * 갑옷 거치대의 생성을 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArmorStandSpawnHandler implements EntitySpawnHandler<ArmorStand> {
    @Getter
    private static final ArmorStandSpawnHandler instance = new ArmorStandSpawnHandler();

    @Override
    @NonNull
    public ArmorStand createEntity(@NonNull Location spawnLocation) {
        return spawnLocation.getWorld().spawn(spawnLocation, ArmorStand.class, armorStand -> {
            armorStand.setAI(false);
            armorStand.setInvulnerable(true);
            armorStand.setSilent(true);
            armorStand.setMarker(true);
            armorStand.setVisible(false);
        });
    }
}
