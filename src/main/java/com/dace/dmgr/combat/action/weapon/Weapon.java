package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.Action;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

/**
 * 무기의 상태를 관리하는 인터페이스.
 *
 * @see AbstractWeapon
 */
public interface Weapon extends Action {
    /**
     * @return 무기 아이템 객체
     */
    @NonNull
    ItemStack getItemStack();

    /**
     * 무기 아이템의 내구도를 변경한다.
     *
     * @param durability 내구도
     */
    void displayDurability(short durability);

    /**
     * 무기 아이템의 발광(마법 부여) 여부를 설정한다.
     *
     * @param isGlowing 아이템 발광(마법 부여) 여부
     */
    void setGlowing(boolean isGlowing);

    /**
     * 무기 아이템의 표시 여부를 설정한다.
     *
     * @param isVisible 아이템 표시 여부
     */
    void setVisible(boolean isVisible);
}
