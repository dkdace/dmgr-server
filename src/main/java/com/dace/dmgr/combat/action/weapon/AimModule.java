package com.dace.dmgr.combat.action.weapon;

import lombok.Getter;

/**
 * 무기의 정조준 모듈 클래스.
 */
public class AimModule {
    /** 무기 객체 */
    private final Weapon weapon;

    /** 정조준 상태 */
    @Getter
    private boolean aiming = false;

    public AimModule(Weapon weapon) {
        this.weapon = weapon;
    }

    /**
     * 무기를 정조준한다.
     *
     * <p>무기가 {@link Aimable}을 상속받는 클래스여야 한다.</p>
     *
     * @see Aimable
     */
    public void aim() {
        if (!aiming) {
            weapon.getCombatUser().getEntity().getEquipment().getItemInMainHand().setDurability((short) (weapon.getItemStack().getDurability() + ((Aimable) weapon).getScope()));
            aiming = true;
        } else {
            weapon.getCombatUser().getEntity().getEquipment().getItemInMainHand().setDurability(weapon.getItemStack().getDurability());
            aiming = false;
        }
    }
}
