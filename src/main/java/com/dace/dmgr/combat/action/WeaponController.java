package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.task.TaskTimer;
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

    public void setCooldown() {

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
        boolean full = remainingAmmo == 0;

        ((Reloadable) weapon).onReload(combatUser, this);
        long duration = ((Reloadable) weapon).getReloadDuration();
        if (remainingAmmo == 0)
            duration = ((Reloadable) weapon).getReloadDurationFull();

        new TaskTimer(1, duration) {
            @Override
            public boolean run(int i) {
                if (!reloading)
                    return false;

                String time = String.format("%.1f", (float) (duration - i) / 20);
                combatUser.getEntity().sendTitle("", "§f재장전 §7[" + time + "초]", 0, 2, 5);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                if (cancelled)
                    return;

                combatUser.getEntity().sendTitle("", "§a재장전 완료", 0, 2, 10);

                remainingAmmo = ((Reloadable) weapon).getCapacity();
                if (!full)
                    remainingAmmo++;
                reloading = false;
            }
        };

    }
}
