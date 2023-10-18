package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

/**
 * 무기의 재장전 모듈 클래스.
 *
 * <p>무기가 {@link Reloadable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Reloadable
 */
public final class ReloadModule<T extends Weapon & Reloadable> {
    /** 무기 객체 */
    private final T weapon;

    /** 남은 탄약 수 */
    @Getter
    @Setter
    private int remainingAmmo;
    /** 재장전 상태 */
    @Getter
    @Setter
    private boolean reloading = false;

    public ReloadModule(T weapon) {
        this.weapon = weapon;
        this.remainingAmmo = weapon.getCapacity();
    }

    /**
     * 지정한 양만큼 무기의 탄약을 소모한다.
     *
     * <p>탄약을 전부 소진하면 {@link Reloadable#reload()}를 호출한다.</p>
     *
     * @param amount 탄약 소모량
     */
    public void consume(int amount) {
        remainingAmmo -= amount;
        if (remainingAmmo < 0)
            remainingAmmo = 0;

        if (reloading)
            reloading = false;
        else if (remainingAmmo == 0)
            weapon.reload();
    }

    /**
     * 무기를 재장전한다.
     */
    public void reload() {
        if (reloading)
            return;
        if (weapon instanceof Swappable && ((Swappable) weapon).getWeaponState() == Swappable.WeaponState.SWAPPING)
            return;

        reloading = true;

        long duration = weapon.getReloadDuration();
        CooldownManager.setCooldown(weapon, Cooldown.WEAPON_RELOAD, duration);

        new TaskTimer(1, duration) {
            @Override
            public boolean run(int i) {
                if (EntityInfoRegistry.getCombatUser(weapon.getCombatUser().getEntity()) == null)
                    return false;
                if (!reloading)
                    return false;
                if (weapon instanceof Aimable && ((Aimable) weapon).isAiming())
                    return false;

                String time = String.format("%.1f", (float) (repeat - i) / 20);
                weapon.getCombatUser().sendActionBar("§c§l재장전... " + StringFormUtil.getProgressBar(i, duration, ChatColor.WHITE) +
                        " §f[" + time + "초]", 2);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                CooldownManager.setCooldown(weapon.getCombatUser(), Cooldown.WEAPON_RELOAD, 0);
                if (cancelled)
                    return;

                weapon.getCombatUser().sendActionBar("§a§l재장전 완료", 8);

                remainingAmmo = weapon.getCapacity();
                reloading = false;
            }
        };
    }
}
