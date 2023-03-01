package com.dace.dmgr.combat.action;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 궁극기 정보를 관리하는 클래스.
 */
public abstract class UltimateSkill extends ActiveSkill {
    public UltimateSkill(String name, String... lore) {
        super(4, name, lore);
        itemStack.setDurability((short) 10);
        itemStack.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);
    }

    @Override
    public long getCooldown() {
        return -1;
    }

    /**
     * 궁극기 필요 충전량을 반환한다.
     *
     * @return 궁극기 필요 충전량
     */
    public abstract int getCost();
}
