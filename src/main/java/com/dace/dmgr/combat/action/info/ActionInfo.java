package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

/**
 * 동작(무기, 패시브 스킬, 액티브 스킬 등)의 표시 아이템을 관리하는 클래스.
 *
 * @see WeaponInfo
 * @see SkillInfo
 * @see TraitInfo
 */
public class ActionInfo {
    /** 이름 */
    protected final String name;
    /** 설명 GUI 아이템 인스턴스 */
    @NonNull
    @Getter
    protected final DefinedItem definedItem;

    /**
     * 동작 정보 인스턴스를 생성한다.
     *
     * @param name           이름
     * @param itemStack      설명 아이템
     * @param actionInfoLore 동작 정보 설명
     */
    protected ActionInfo(@NonNull String name, @NonNull ItemStack itemStack, @NonNull ActionInfoLore actionInfoLore) {
        this.name = name;
        this.definedItem = new DefinedItem(new ItemBuilder(itemStack).setLore(actionInfoLore.toString()).build());
    }
}
