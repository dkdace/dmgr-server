package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class WeaponController {
    private final CombatUser combatUser;
    private final Weapon weapon;
    private final ItemStack itemStack;
    private int remainingAmmo = -1;
    private boolean reloading = false;

    public WeaponController(CombatUser combatUser, Weapon weapon) {
        this.combatUser = combatUser;
        this.weapon = weapon;
        this.itemStack = weapon.getItemStack().clone();
        if (weapon instanceof Reloadable)
            this.remainingAmmo = ((Reloadable) weapon).getCapacity();
        apply();
    }

    public void apply() {
        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }

    public void setCooldown(int cooldown, boolean force) {
        if (cooldown == -1)
            cooldown = 9999;
        if (force || cooldown > combatUser.getEntity().getCooldown(Weapon.MATERIAL))
            combatUser.getEntity().setCooldown(Weapon.MATERIAL, cooldown);
    }

    public void setCooldown(int cooldown) {
        setCooldown(cooldown, false);
    }

    public void setCooldown() {
        setCooldown((int) weapon.getCooldown());
    }

    public boolean isCooldownFinished() {
        return combatUser.getEntity().getCooldown(Weapon.MATERIAL) == 0;
    }

    public boolean isReloading() {
        return reloading;
    }

    public void setReloading(boolean reloading) {
        this.reloading = reloading;
    }

    public int getRemainingAmmo() {
        return remainingAmmo;
    }

    public void setRemainingAmmo(int remainingAmmo) {
        this.remainingAmmo = remainingAmmo;
    }

    public void consume(int amount) {
        remainingAmmo -= amount;
        if (remainingAmmo < 0)
            remainingAmmo = 0;
    }

    public void reload() {
        if (!(weapon instanceof Reloadable))
            return;

        if (remainingAmmo >= ((Reloadable) weapon).getCapacity())
            return;
        if (reloading)
            return;

        reloading = true;

        long duration = ((Reloadable) weapon).getReloadDuration();
        CooldownManager.setCooldown(combatUser, Cooldown.WEAPON_RELOAD, duration);

        new TaskTimer(1, duration) {
            @Override
            public boolean run(int i) {
                if (!reloading)
                    return false;

                String time = String.format("%.1f", (float) (repeat - i) / 20);
                combatUser.sendActionBar("§c§l재장전... " + StringUtil.getBar(i, duration, ChatColor.WHITE) + " §f[" + time + "초]",
                        2);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                CooldownManager.setCooldown(combatUser, Cooldown.WEAPON_RELOAD, 0);
                if (cancelled)
                    return;

                combatUser.sendActionBar("§a§l재장전 완료", 8);

                remainingAmmo = ((Reloadable) weapon).getCapacity();
                reloading = false;
            }
        };

    }
}
