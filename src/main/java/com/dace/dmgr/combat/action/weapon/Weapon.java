package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.entity.CombatUser;

/**
 * 무기의 상태를 관리하는 클래스.
 */
public abstract class Weapon extends Action {
    /**
     * 무기 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     * @param weaponInfo 무기 정보 객체
     */
    protected Weapon(CombatUser combatUser, WeaponInfo weaponInfo) {
        super(combatUser, weaponInfo);
        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }

    @Override
    protected void onCooldownSet() {
        combatUser.getEntity().setCooldown(WeaponInfo.MATERIAL, (int) getCooldown());
    }

    /**
     * 스킬 설명 아이템의 내구도를 변경한다.
     *
     * @param durability 내구도
     */
    public final void displayDurability(short durability) {
        itemStack.setDurability(durability);
        display();
    }

    /**
     * 무기 설명 아이템을 적용한다.
     */
    private void display() {
        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }
}
