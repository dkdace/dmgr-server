package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class WeaponController {
    private final static Material MATERIAL = Material.DIAMOND_HOE;
    private final CombatUser combatUser;
    private final Weapon weapon;
    private final ItemStack itemStack;

    public WeaponController(CombatUser combatUser, Weapon weapon) {
        this.combatUser = combatUser;
        this.weapon = weapon;
        this.itemStack = new ItemBuilder(weapon.getItemStack()).build();
        apply();
    }

    private void apply() {
        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }
}
