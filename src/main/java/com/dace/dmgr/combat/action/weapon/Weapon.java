package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.Action;

/**
 * 무기의 상태를 관리하는 인터페이스.
 *
 * @see AbstractWeapon
 */
public interface Weapon extends Action {
    /**
     * 무기 아이템의 내구도를 변경한다.
     *
     * @param durability 내구도
     */
    void displayDurability(short durability);
}
