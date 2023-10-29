package com.dace.dmgr.combat.action.skill.module;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.LocationConfirmable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.inventivetalent.glow.GlowAPI;

import java.text.MessageFormat;

/**
 * 스킬의 위치 확인 모듈 클래스.
 *
 * <p>스킬이 {@link LocationConfirmable}을 상속받는 클래스여야 한다.</p>
 *
 * @see LocationConfirmable
 */
public final class LocationConfirmModule extends ConfirmModule {
    /** 최대 거리 */
    private final int maxDistance;

    /** 현재 지정 위치 */
    @Getter
    private Location currentLocation;
    /** 위치 표시용 갑옷 거치대 객체 */
    private ArmorStand pointer;

    public LocationConfirmModule(LocationConfirmable skill, ActionKey acceptKey, ActionKey cancelKey, int maxDistance) {
        super(skill, acceptKey, cancelKey);
        this.maxDistance = maxDistance;
    }

    /**
     * 위치를 지정할 수 있는 지 확인한다.
     *
     * @return 위치 지정 가능 여부
     */
    public boolean isValid() {
        if (!isChecking)
            return false;
        return currentLocation.getBlock().isEmpty() && !currentLocation.clone().add(0, -1, 0).getBlock().isEmpty();
    }

    @Override
    protected void onCheckEnable() {
        Player player = skill.getCombatUser().getEntity();

        pointer = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        pointer.setSilent(true);
        pointer.setInvulnerable(true);
        pointer.setGravity(false);
        pointer.setAI(false);
        pointer.setMarker(true);
        pointer.setVisible(false);
        pointer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false,
                false), true);
        pointer.setHelmet(new ItemStack(Material.HOPPER));

        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(new int[]{pointer.getEntityId()});
        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            if (player != player2)
                packet.sendPacket(player2);
        });
    }

    @Override
    protected void onCheckTick(int i) {
        Player player = skill.getCombatUser().getEntity();

        currentLocation = player.getTargetBlock(null, maxDistance).getLocation().add(0.5, 1, 0.5);
        currentLocation.setYaw(i * 10);
        currentLocation.setPitch(0);

        pointer.teleport(currentLocation.clone().add(0, -1.75, 0).add(currentLocation.getDirection().multiply(0.25)));
        if (isValid()) {
            GlowAPI.setGlowing(pointer, GlowAPI.Color.GREEN, player);
            skill.getCombatUser().getEntity().sendTitle("", MessageFormat.format(MESSAGES.CHECKING_VALID, acceptKey.getName(),
                    cancelKey.getName()), 0, 5, 5);
        } else {
            GlowAPI.setGlowing(pointer, GlowAPI.Color.RED, player);
            skill.getCombatUser().getEntity().sendTitle("", MessageFormat.format(MESSAGES.CHECKING_INVALID, acceptKey.getName(),
                    cancelKey.getName()), 0, 5, 5);
        }
        pointer.setAI(false);
    }

    @Override
    protected void onCheckDisable() {
        pointer.remove();
    }

    @Override
    public void onReset() {
        pointer.remove();
    }

    @Override
    public void onRemove() {
        pointer.remove();
    }

    /**
     * 메시지 목록.
     */
    private static class MESSAGES {
        /** 확인 중 메시지 (사용 가능) */
        static final String CHECKING_VALID = "§7§l[{0}] §f설치     §7§l[{1}] §f취소";
        /** 확인 중 메시지 (사용 불가능) */
        static final String CHECKING_INVALID = "§7§l[{0}] §c설치     §7§l[{1}] §f취소";
    }
}
