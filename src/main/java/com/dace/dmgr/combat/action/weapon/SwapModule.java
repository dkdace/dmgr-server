package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.StringFormUtil;
import org.bukkit.ChatColor;

/**
 * 무기의 2중 무기 모듈 클래스.
 */
public class SwapModule {
    /** 무기 객체 */
    private final Weapon weapon;

    public SwapModule(Weapon weapon) {
        this.weapon = weapon;
    }

    /**
     * 이중 무기의 상태를 변경한다.
     *
     * <p>무기가 {@link Swappable}을 상속받는 클래스여야 한다.</p>
     *
     * @param targetState 변경할 상태
     * @see Swappable
     */
    private void swapTo(WeaponState targetState) {
        if (weapon.getWeaponState() == targetState || weapon.getWeaponState() == WeaponState.SWAPPING)
            return;
        if (targetState == WeaponState.SWAPPING)
            return;

        ((Reloadable) weapon).cancelReloading();
        if (weapon.getWeaponState() == WeaponState.SECONDARY)
            ((Reloadable) ((Swappable) weapon).getSubweapon()).cancelReloading();
        weapon.setWeaponState(WeaponState.SWAPPING);

        long duration = ((Swappable) weapon).getSwapDuration();
        CooldownManager.setCooldown(weapon.getCombatUser(), Cooldown.WEAPON_SWAP, duration);

        new TaskTimer(1, duration) {
            @Override
            public boolean run(int i) {
                if (weapon.getWeaponState() != WeaponState.SWAPPING)
                    return false;

                String time = String.format("%.1f", (float) (repeat - i) / 20);
                weapon.getCombatUser().sendActionBar("§c§l무기 교체 중... " + StringFormUtil.getProgressBar(i, duration, ChatColor.WHITE) +
                        " §f[" + time + "초]", 2);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                CooldownManager.setCooldown(weapon.getCombatUser(), Cooldown.WEAPON_RELOAD, 0);
                if (cancelled)
                    return;

                weapon.getCombatUser().sendActionBar("§a§l무기 교체 완료", 8);
                weapon.setWeaponState(targetState);

                // remainingAmmo와 oppositeAmmo 교체 (XOR algorithm)
                //remainingAmmo = remainingAmmo ^ oppositeAmmo ^ (oppositeAmmo = remainingAmmo);

                //if (targetState == WeaponState.PRIMARY)
                //    apply();
                //else
                //    applySubweapon();
            }
        };
    }

    /**
     * 이중 무기의 모드를 반대 무기로 교체한다.
     *
     * <p>무기가 {@link Swappable}을 상속받는 클래스여야 한다.</p>
     *
     * @see Swappable
     */
    public void swap() {
        if (weapon.getWeaponState() == WeaponState.PRIMARY)
            swapTo(WeaponState.SECONDARY);
        else if (weapon.getWeaponState() == WeaponState.SECONDARY)
            swapTo(WeaponState.PRIMARY);
    }
}