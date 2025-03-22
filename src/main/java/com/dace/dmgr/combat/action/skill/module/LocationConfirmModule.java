package com.dace.dmgr.combat.action.skill.module;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.Confirmable;
import com.dace.dmgr.util.LocationUtil;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

/**
 * 스킬의 위치 확인 모듈 클래스.
 *
 * @see Confirmable
 */
public final class LocationConfirmModule extends ConfirmModule {
    /** 최대 거리. (단위: 블록) */
    private final int maxDistance;

    /** 현재 지정 위치 */
    private Location currentLocation;
    /** 위치 표시용 갑옷 거치대 인스턴스 */
    @Nullable
    private ArmorStand pointer = null;

    /**
     * 위치 확인 모듈 인스턴스를 생성한다.
     *
     * @param skill       대상 스킬
     * @param acceptKey   수락 키
     * @param cancelKey   취소 키
     * @param maxDistance 최대 거리. (단위: 블록). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public LocationConfirmModule(@NonNull Confirmable skill, @NonNull ActionKey acceptKey, @NonNull ActionKey cancelKey, int maxDistance) {
        super(skill, acceptKey, cancelKey);
        Validate.isTrue(maxDistance >= 0, "maxDistance >= 0 (%d)", maxDistance);

        this.maxDistance = maxDistance;
        this.currentLocation = skill.getCombatUser().getLocation();

        skill.addOnRemove(() -> {
            if (pointer != null)
                pointer.remove();
        });
    }

    /**
     * 위치를 지정할 수 있는지 확인한다.
     *
     * @return 위치 지정 가능 여부
     */
    public boolean isValid() {
        Player player = skill.getCombatUser().getEntity();
        if (!isChecking || currentLocation.equals(player.getLocation())
                || !currentLocation.getBlock().isEmpty()
                || currentLocation.clone().subtract(0, 1, 0).getBlock().isEmpty())
            return false;

        Location loc = player.getEyeLocation();
        loc.setY(currentLocation.getY());

        return player.getEyeLocation().getY() > currentLocation.getY() || LocationUtil.canPass(loc, player.getEyeLocation());
    }

    /**
     * 현재 지정 위치를 반환한다.
     *
     * @return 현재 지정 위치
     */
    @NonNull
    public Location getCurrentLocation() {
        return currentLocation.clone();
    }

    @Override
    protected void onCheckEnable() {
        Player player = skill.getCombatUser().getEntity();

        pointer = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        pointer.setCustomName(DMGR.TEMPORARY_ENTITY_CUSTOM_NAME);
        pointer.setSilent(true);
        pointer.setInvulnerable(true);
        pointer.setGravity(false);
        pointer.setAI(false);
        pointer.setMarker(true);
        pointer.setVisible(false);
        pointer.addPotionEffect(
                new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false), true);
        pointer.setHelmet(new ItemStack(Material.HOPPER));
        currentLocation = skill.getCombatUser().getLocation();

        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(new int[]{pointer.getEntityId()});

        player.getWorld().getPlayers().forEach(target -> {
            if (player != target)
                packet.sendPacket(target);
        });
    }

    @Override
    protected void onCheckTick(long i) {
        Validate.notNull(pointer);

        currentLocation = skill.getCombatUser().getEntity().getTargetBlock(null, maxDistance).getLocation().add(0.5, 0.5, 0.5);
        if (LocationUtil.isNonSolid(currentLocation))
            currentLocation = LocationUtil.getNearestAgainstEdge(currentLocation, new Vector(0, -1, 0), 8);
        if (!LocationUtil.isNonSolid(currentLocation))
            currentLocation = LocationUtil.getNearestAgainstEdge(currentLocation, new Vector(0, 1, 0), 8);

        if (!currentLocation.getBlock().isEmpty())
            currentLocation.add(0, 0.5, 0);
        currentLocation.setYaw(i * 10F);
        currentLocation.setPitch(0);

        pointer.teleport(currentLocation.clone().add(0, -1.75, 0).add(currentLocation.getDirection().multiply(0.25)));
        pointer.setAI(false);

        String message = MessageFormat.format("§7§l[{0}] {1}설치     §7§l[{2}] §f취소",
                acceptKey,
                (isValid() ? ChatColor.WHITE : ChatColor.RED),
                cancelKey);

        skill.getCombatUser().getUser().sendTitle("", message, Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(5));
        skill.getCombatUser().getUser().setGlowing(pointer, (isValid() ? ChatColor.GREEN : ChatColor.RED));
    }

    @Override
    protected void onCheckDisable() {
        Validate.notNull(pointer).remove();
    }
}
