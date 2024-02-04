package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.AbstractAction;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * {@link Weapon}의 기본 구현체, 모든 무기의 기반 클래스.
 */
public abstract class AbstractWeapon extends AbstractAction implements Weapon {
    /**
     * 무기 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     * @param weaponInfo 무기 정보 객체
     */
    protected AbstractWeapon(@NonNull CombatUser combatUser, @NonNull WeaponInfo weaponInfo) {
        super(combatUser, weaponInfo);

        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }

    @Override
    protected void onCooldownSet() {
        combatUser.getEntity().setCooldown(WeaponInfo.MATERIAL, (int) getCooldown());
    }

    @Override
    public boolean canUse() {
        return super.canUse() && combatUser.isGlobalCooldownFinished();
    }

    @Override
    @MustBeInvokedByOverriders
    public void dispose() {
        super.dispose();

        combatUser.getEntity().getInventory().clear(4);
    }

    @Override
    public final void displayDurability(short durability) {
        itemStack.setDurability(durability);
        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }
}
