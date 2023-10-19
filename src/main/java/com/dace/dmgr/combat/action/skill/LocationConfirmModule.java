package com.dace.dmgr.combat.action.skill;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.dace.dmgr.combat.action.ActionKey;
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

/**
 * 스킬의 위치 확인 모듈 클래스.
 *
 * <p>스킬이 {@link LocationConfirmModule}을 상속받는 클래스여야 한다.</p>
 *
 * @see LocationConfirmable
 */
public final class LocationConfirmModule<T extends Skill & LocationConfirmable> extends ConfirmModule<T> {
    /** 현재 위치 */
    @Getter
    private Location location;
    /** 위치 표시용 갑옷 거치대 객체 */
    private ArmorStand pointer;
    /** 위치 지정 가능 여부 */
    @Getter
    private boolean valid = false;

    public LocationConfirmModule(T skill, ActionKey confirmKey, ActionKey cancelKey) {
        super(skill, confirmKey, cancelKey);
    }

    @Override
    protected void onEnable() {
        Player player = skill.getCombatUser().getEntity();

        pointer = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        pointer.setSilent(true);
        pointer.setInvulnerable(true);
        pointer.setGravity(false);
        pointer.setAI(false);
        pointer.setMarker(true);
        pointer.setVisible(false);
        pointer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false, false), true);
        pointer.setHelmet(new ItemStack(Material.HOPPER));

        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(new int[]{pointer.getEntityId()});
        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            if (player != player2)
                packet.sendPacket(player2);
        });
    }

    @Override
    protected void onTick(int i) {
        Player player = skill.getCombatUser().getEntity();

        location = player.getTargetBlock(null, skill.getMaxDistance()).getLocation().add(0.5, 1, 0.5);
        location.setYaw(i * 10);
        location.setPitch(0);

        pointer.teleport(location.clone().add(0, -1.75, 0).add(location.getDirection().multiply(0.25)));
        if (location.getBlock().isEmpty() && !location.clone().add(0, -1, 0).getBlock().isEmpty()) {
            GlowAPI.setGlowing(pointer, GlowAPI.Color.GREEN, player);
            skill.getCombatUser().getEntity().sendTitle("", "§7§l[" + confirmKey.getName() + "] §f설치     " + "§7§l[" + cancelKey.getName() + "] §f취소",
                    0, 5, 5);
            valid = true;
        } else {
            GlowAPI.setGlowing(pointer, GlowAPI.Color.RED, player);
            skill.getCombatUser().getEntity().sendTitle("", "§7§l[" + confirmKey.getName() + "] §c설치     " + "§7§l[" + cancelKey.getName() + "] §f취소",
                    0, 5, 5);
            valid = false;
        }
        pointer.setAI(false);
    }

    @Override
    protected void onDisable() {
        pointer.remove();
        valid = false;
    }
}
