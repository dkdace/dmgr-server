package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.Setter;

/**
 * 무기의 상태를 관리하는 클래스.
 */
@Getter
public abstract class Weapon extends Action {
    /** 무기의 상태 */
    @Setter
    protected WeaponState weaponState = WeaponState.PRIMARY;

    /**
     * 무기 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     * @param weaponInfo 무기 정보 객체
     */
    protected Weapon(CombatUser combatUser, WeaponInfo weaponInfo) {
        super(combatUser, weaponInfo);
        apply();
    }

    /**
     * 무기의 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     */
    @Override
    public void setCooldown(long cooldown) {
        setCooldown(cooldown, false);
    }

    /**
     * 무기의 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     * @param force    덮어쓰기 여부
     */
    public void setCooldown(long cooldown, boolean force) {
        if (cooldown == -1)
            cooldown = 9999;
        if (force || cooldown > combatUser.getEntity().getCooldown(WeaponInfo.MATERIAL))
            combatUser.getEntity().setCooldown(WeaponInfo.MATERIAL, (int) cooldown);
    }

    /**
     * 무기의 쿨타임을 무기에 설정된 기본 쿨타임으로 설정한다.
     *
     * <p>보조무기 상태라면 보조무기의 기본 쿨타임으로 설정한다.</p>
     *
     * @see Weapon#getCooldown()
     */
    public void setCooldown() {
        if (weaponState == WeaponState.PRIMARY)
            setCooldown((int) getCooldown());
        else if (weaponState == WeaponState.SECONDARY)
            setCooldown((int) ((Swappable) this).getSubweapon().getCooldown());
    }

    /**
     * 무기의 쿨타임이 끝났는 지 확인한다.
     *
     * @return 쿨타임 종료 여부
     */
    @Override
    public boolean isCooldownFinished() {
        return combatUser.getEntity().getCooldown(WeaponInfo.MATERIAL) == 0;
    }

    /**
     * 플레이어의 인벤토리에 무기 아이템을 적용한다.
     */
    @Override
    public void apply() {
        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }
}
