package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 궁극기 정보를 관리하는 클래스.
 */
public abstract class UltimateSkillInfo extends ActiveSkillInfo {
    /**
     * 궁극기 정보 인스턴스를 생성한다.
     *
     * @param name  이름
     * @param lores 설명 목록
     */
    protected UltimateSkillInfo(@NonNull String name, @NonNull String @NonNull ... lores) {
        super(4, name, lores);
        itemStack.setDurability((short) 10);
        itemStack.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);
    }

    @Override
    public String toString() {
        return "§d［" + name + "］";
    }

    @Override
    @NonNull
    public abstract UltimateSkill createSkill(@NonNull CombatUser combatUser);
}
