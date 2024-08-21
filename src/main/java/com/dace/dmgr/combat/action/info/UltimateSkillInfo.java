package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.combat.action.skill.UltimateSkill;
import lombok.NonNull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 궁극기 정보를 관리하는 클래스.
 *
 * @param <T> {@link UltimateSkill}을 상속받는 궁극기
 */
public abstract class UltimateSkillInfo<T extends UltimateSkill> extends ActiveSkillInfo<T> {
    /**
     * 궁극기 정보 인스턴스를 생성한다.
     *
     * @param skillClass 궁극기 스킬 클래스
     * @param name       이름
     * @param lores      설명 목록
     */
    protected UltimateSkillInfo(@NonNull Class<@NonNull T> skillClass, @NonNull String name, @NonNull String @NonNull ... lores) {
        super(skillClass, name, lores);
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
}
