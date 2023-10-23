package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.ActionBase;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;

/**
 * 모든 무기의 기반 클래스.
 */
public abstract class WeaponBase extends ActionBase implements Weapon {
    /**
     * 무기 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     * @param weaponInfo 무기 정보 객체
     */
    protected WeaponBase(CombatUser combatUser, WeaponInfo weaponInfo) {
        super(combatUser, weaponInfo);
        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }

    @Override
    public void onCooldownSet() {
        combatUser.getEntity().setCooldown(WeaponInfo.MATERIAL, (int) getCooldown());
    }

    @Override
    public void onCooldownTick() {
    }

    @Override
    public void onCooldownFinished() {
    }

    @Override
    public boolean canUse() {
        return super.canUse() && combatUser.isGlobalCooldownFinished();
    }

    @Override
    public final void displayDurability(short durability) {
        itemStack.setDurability(durability);
        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }
}
