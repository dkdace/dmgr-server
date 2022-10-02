package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.inventory.ItemStack;

public class WeaponController {
    private final CombatUser combatUser;
    private final Weapon weapon;
    private final ItemStack itemStack;

    public WeaponController(CombatUser combatUser, Weapon weapon) {
        this.combatUser = combatUser;
        this.weapon = weapon;
        this.itemStack = weapon.getItemStack().clone();
        apply();
    }

    public void apply() {
        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }
}
