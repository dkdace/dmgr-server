package com.dace.dmgr.combat.action.skill;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.inventivetalent.glow.GlowAPI;

/**
 * 사용 전 위치 확인이 필요한 스킬의 인터페이스.
 */
public interface LocationConfirmable extends Confirmable {
    /**
     * @return 현재 지정 위치
     */
    Location getCurrentLocation();

    /**
     * @param currentLocation 현재 지정 위치
     */
    void setCurrentLocation(Location currentLocation);

    /**
     * @return 위치 표시용 갑옷 거치대 객체
     */
    ArmorStand getPointer();

    /**
     * @param armorStand 위치 표시용 갑옷 거치대 객체
     */
    void setPointer(ArmorStand armorStand);

    /**
     * @return 최대 거리
     */
    int getMaxDistance();

    /**
     * 위치를 지정할 수 있는 지 확인한다.
     *
     * @return 위치 지정 가능 여부
     */
    default boolean isValid() {
        return getCurrentLocation().getBlock().isEmpty() && !getCurrentLocation().clone().add(0, -1, 0).getBlock().isEmpty();
    }

    @Override
    default void onCheckEnable() {
        Player player = getCombatUser().getEntity();

        setPointer(player.getWorld().spawn(player.getLocation(), ArmorStand.class));
        getPointer().setSilent(true);
        getPointer().setInvulnerable(true);
        getPointer().setGravity(false);
        getPointer().setAI(false);
        getPointer().setMarker(true);
        getPointer().setVisible(false);
        getPointer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false,
                false), true);
        getPointer().setHelmet(new ItemStack(Material.HOPPER));

        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(new int[]{getPointer().getEntityId()});
        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            if (player != player2)
                packet.sendPacket(player2);
        });
    }

    @Override
    default void onCheckTick(int i) {
        Player player = getCombatUser().getEntity();

        setCurrentLocation(player.getTargetBlock(null, getMaxDistance()).getLocation().add(0.5, 1, 0.5));
        getCurrentLocation().setYaw(i * 10);
        getCurrentLocation().setPitch(0);

        getPointer().teleport(getCurrentLocation().clone().add(0, -1.75, 0).add(getCurrentLocation().getDirection().multiply(0.25)));
        if (isValid()) {
            GlowAPI.setGlowing(getPointer(), GlowAPI.Color.GREEN, player);
            getCombatUser().getEntity().sendTitle("", "§7§l[" + getAcceptKey().getName() + "] §f설치     " + "§7§l[" +
                            getCancelKey().getName() + "] §f취소",
                    0, 5, 5);
        } else {
            GlowAPI.setGlowing(getPointer(), GlowAPI.Color.RED, player);
            getCombatUser().getEntity().sendTitle("", "§7§l[" + getAcceptKey().getName() + "] §c설치     " + "§7§l[" +
                            getCancelKey().getName() + "] §f취소",
                    0, 5, 5);
        }
        getPointer().setAI(false);
    }

    @Override
    default void onCheckDisable() {
        getPointer().remove();
    }
}