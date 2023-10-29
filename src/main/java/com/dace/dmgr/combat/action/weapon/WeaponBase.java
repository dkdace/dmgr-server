package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.ActionBase;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 모든 무기의 기반 클래스.
 */
public abstract class WeaponBase extends ActionBase implements Weapon {
    /**
     * 무기 인스턴스를 생성한다.
     *
     * <p>{@link WeaponBase#init()}을 호출하여 초기화해야 한다.</p>
     *
     * @param combatUser 대상 플레이어
     * @param weaponInfo 무기 정보 객체
     */
    protected WeaponBase(CombatUser combatUser, WeaponInfo weaponInfo) {
        super(combatUser, weaponInfo);
    }

    @Override
    public void init() {
        super.init();

        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }

    @Override
    protected void onCooldownSet() {
        combatUser.getEntity().setCooldown(WeaponInfo.MATERIAL, (int) getCooldown());
    }

    @Override
    protected void onCooldownTick() {
    }

    @Override
    protected void onCooldownFinished() {
    }

    @Override
    public boolean canUse() {
        return super.canUse() && combatUser.isGlobalCooldownFinished();
    }

    @Override
    @MustBeInvokedByOverriders
    public void remove() {
        super.remove();

        combatUser.getEntity().getInventory().clear(4);
    }

    @Override
    public final void displayDurability(short durability) {
        itemStack.setDurability(durability);
        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }
}
