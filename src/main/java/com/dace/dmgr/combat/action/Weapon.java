package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class Weapon extends Action {
    private final static Material material = Material.DIAMOND_HOE;
    private static final String PREFIX = "§e§l[기본무기] §f";

    public Weapon(String name, ItemStack itemStack) {
        super(name, itemStack);
    }

    public abstract long getCooldown();

    public abstract void use(CombatUser combatUser, WeaponController weaponController);
}
