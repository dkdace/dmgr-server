package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.item.DefinedItem;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

/**
 * 동작(무기, 패시브 스킬, 액티브 스킬 등) 정보를 관리하는 클래스.
 *
 * @see WeaponInfo
 * @see SkillInfo
 * @see TraitInfo
 */
public class ActionInfo {
    /** 이름 */
    @Getter
    protected final String name;
    /** 설명 정적 아이템 객체 */
    @Getter
    protected final DefinedItem definedItem;
    /** 설명 아이템 객체 */
    protected final ItemStack itemStack;

    /**
     * 동작 정보 인스턴스를 생성한다.
     *
     * @param name       이름
     * @param definedItem 설명 정적 아이템 객체
     */
    protected ActionInfo(@NonNull String name, @NonNull DefinedItem definedItem) {
        this.name = name;
        this.definedItem = definedItem;
        this.itemStack = definedItem.getItemStack();
    }
}
