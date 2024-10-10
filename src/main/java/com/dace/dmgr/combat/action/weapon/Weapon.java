package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.Action;
import lombok.NonNull;
import org.bukkit.Material;

/**
 * 무기의 상태를 관리하는 인터페이스.
 *
 * @see AbstractWeapon
 */
public interface Weapon extends Action {
    /**
     * 무기 아이템의 타입을 변경한다.
     *
     * @param material 아이템 타입
     */
    void displayMaterial(@NonNull Material material);

    /**
     * 무기 아이템의 내구도를 변경한다.
     *
     * @param durability 내구도. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
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
