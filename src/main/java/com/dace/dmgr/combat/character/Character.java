package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.Weapon;
import org.bukkit.inventory.ItemStack;

public class Character {
    protected String name;
    protected ItemStack weapon;
    protected int health;
    protected float speed;
    protected float hitbox;
    protected String skinName;

    public Character(String name, Weapon weapon, int health, float speed, float hitbox, String skinName) {
        this.name = name;
        this.weapon = weapon.getItemStack();
        this.health = health;
        this.speed = speed;
        this.hitbox = hitbox;
        this.skinName = skinName;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public float getSpeed() {
        return speed;
    }

    public float getHitbox() {
        return hitbox;
    }

    public String getSkinName() {
        return skinName;
    }
}
