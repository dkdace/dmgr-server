package com.dace.dmgr.combat.entity;

import com.dace.dmgr.gui.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * 더미(훈련용 봇) 엔티티 클래스.
 */
public class Dummy extends TemporalEntity<Zombie> {
    /**
     * 더미 인스턴스를 생성한다.
     *
     * @param maxHealth 최대 체력
     */
    public Dummy(Zombie entity, int maxHealth) {
        super(
                entity,
                "§7§lDummy",
                new Hitbox(0, 0.9, 0, 0.65, 2.1, 0.5),
                new Hitbox(0, 2, 0, 0.3, 0.1, 0.3),
                true,
                maxHealth
        );
    }

    @Override
    protected void onInitTemporalEntity(Location location) {
        setTeam("DUMMY");
        entity.setBaby(false);
        entity.leaveVehicle();
        entity.setAI(false);

        List<ItemStack> equipment = new ArrayList<>();
        equipment.add(new ItemBuilder(Material.LEATHER_CHESTPLATE).build());
        equipment.add(new ItemBuilder(Material.LEATHER_LEGGINGS).build());
        equipment.add(new ItemBuilder(Material.LEATHER_BOOTS).build());
        equipment.forEach((ItemStack itemStack) -> {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
            leatherArmorMeta.setColor(Color.fromRGB(255, 255, 255));
            itemStack.setItemMeta(leatherArmorMeta);
        });

        entity.getEquipment().setHelmet(new ItemBuilder(Material.STAINED_GLASS).setDamage((short) 15).build());
        entity.getEquipment().setChestplate(equipment.get(0));
        entity.getEquipment().setLeggings(equipment.get(1));
        entity.getEquipment().setBoots(equipment.get(2));
        entity.addPotionEffect(
                new PotionEffect(PotionEffectType.SLOW, 99999, 5, false, false));
    }

    @Override
    public boolean isUltProvider() {
        return true;
    }
}
