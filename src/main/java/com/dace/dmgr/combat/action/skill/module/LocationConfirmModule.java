package com.dace.dmgr.combat.action.skill.module;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.Confirmable;
import com.dace.dmgr.util.GlowUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.MessageFormat;

/**
 * 스킬의 위치 확인 모듈 클래스.
 *
 * <p>스킬이 {@link Confirmable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Confirmable
 */
public final class LocationConfirmModule extends ConfirmModule {
    /** 최대 거리. (단위: 블록) */
    private final int maxDistance;

    /** 현재 지정 위치 */
    @NonNull
    @Getter
    private Location currentLocation;
    /** 위치 표시용 갑옷 거치대 객체 */
    private ArmorStand pointer = null;

    /**
     * 위치 확인 모듈 인스턴스를 생성한다.
     *
     * @param skill       대상 스킬
     * @param acceptKey   수락 키
     * @param cancelKey   취소 키
     * @param maxDistance 최대 거리. (단위: 블록)
     */
    public LocationConfirmModule(@NonNull Confirmable skill, @NonNull ActionKey acceptKey, @NonNull ActionKey cancelKey, int maxDistance) {
        super(skill, acceptKey, cancelKey);
        this.maxDistance = maxDistance;
        this.currentLocation = skill.getCombatUser().getEntity().getLocation();

        new IntervalTask(i -> !skill.isDisposed(), isCancelled -> {
            if (pointer != null)
                pointer.remove();
        }, 1);
    }

    /**
     * 위치를 지정할 수 있는 지 확인한다.
     *
     * @return 위치 지정 가능 여부
     */
    public boolean isValid() {
        if (!isChecking)
            return false;
        return !currentLocation.equals(skill.getCombatUser().getEntity().getLocation()) && currentLocation.getBlock().isEmpty() &&
                !currentLocation.clone().add(0, -1, 0).getBlock().isEmpty();
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
        pointer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false,
                false), true);
        pointer.setHelmet(new ItemStack(Material.HOPPER));
        currentLocation = skill.getCombatUser().getEntity().getLocation();

        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(new int[]{pointer.getEntityId()});
        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            if (player != player2)
                packet.sendPacket(player2);
        });
    }

    @Override
    protected void onCheckTick(long i) {
        Player player = skill.getCombatUser().getEntity();

        currentLocation = player.getTargetBlock(null, maxDistance).getLocation().add(0.5, 1, 0.5);
        currentLocation.setYaw(i * 10);
        currentLocation.setPitch(0);

        pointer.teleport(currentLocation.clone().add(0, -1.75, 0).add(currentLocation.getDirection().multiply(0.25)));
        if (isValid()) {
            GlowUtil.setGlowing(pointer, ChatColor.GREEN, player);
            skill.getCombatUser().getUser().sendTitle("", MessageFormat.format("§7§l[{0}] §f설치     §7§l[{1}] §f취소",
                    acceptKey.getName(), cancelKey.getName()), 0, 5, 5);
        } else {
            GlowUtil.setGlowing(pointer, ChatColor.RED, player);
            skill.getCombatUser().getUser().sendTitle("", MessageFormat.format("§7§l[{0}] §c설치     §7§l[{1}] §f취소",
                    acceptKey.getName(), cancelKey.getName()), 0, 5, 5);
        }
        pointer.setAI(false);
    }

    @Override
    protected void onCheckDisable() {
        pointer.remove();
    }
}
