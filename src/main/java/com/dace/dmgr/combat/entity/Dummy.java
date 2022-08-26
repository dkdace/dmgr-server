package com.dace.dmgr.combat.entity;

import com.dace.dmgr.gui.ItemGenerator;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Dummy extends TemporalEntity<Zombie> {
    public Dummy(Location location, int health) {
        super(EntityType.ZOMBIE, "§7§lDummy", location);
        setMaxHealth(health);
        setHealth(health);
        setTeam("DUMMY");

        List<ItemStack> equipment = new ArrayList<>();
        equipment.add(ItemGenerator.getItem(Material.LEATHER_CHESTPLATE, ""));
        equipment.add(ItemGenerator.getItem(Material.LEATHER_LEGGINGS, ""));
        equipment.add(ItemGenerator.getItem(Material.LEATHER_BOOTS, ""));
        equipment.forEach((ItemStack itemStack) -> {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
            leatherArmorMeta.setColor(Color.fromRGB(255, 255, 255));
            itemStack.setItemMeta(leatherArmorMeta);
        });

        entity.getEquipment().setHelmet(ItemGenerator.getItem(Material.STAINED_GLASS, 1, (short) 15, ""));
        entity.getEquipment().setChestplate(equipment.get(0));
        entity.getEquipment().setLeggings(equipment.get(1));
        entity.getEquipment().setBoots(equipment.get(2));
        entity.addPotionEffect(
                new PotionEffect(PotionEffectType.SLOW, 99999, 5, false, false));
    }
}
