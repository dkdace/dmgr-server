package com.dace.dmgr.combat.ability.info;

import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

/**
 * 능력(무기, 스킬, 특성 등)의 정보 표시 아이템을 관리하는 클래스.
 *
 * @see WeaponInfo
 * @see SkillInfo
 * @see TraitInfo
 */
public class AbilityInfo {
    /** 이름 */
    @NonNull
    @Getter
    private final String name;
    /** 설명 GUI 아이템 인스턴스 */
    @NonNull
    @Getter
    private final DefinedItem definedItem;

    /**
     * 능력 정보 인스턴스를 생성한다.
     *
     * @param name            이름
     * @param itemStack       설명 아이템
     * @param abilityInfoLore 능력 정보 설명
     */
    protected AbilityInfo(@NonNull String name, @NonNull ItemStack itemStack, @NonNull AbilityInfoLore abilityInfoLore) {
        this.name = name;
        this.definedItem = new DefinedItem(new ItemBuilder(itemStack).setLore(abilityInfoLore.toString()).build());
    }
}
