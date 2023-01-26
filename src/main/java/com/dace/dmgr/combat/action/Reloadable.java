package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;

/**
 * 재장전 가능한 무기의 인터페이스.
 */
public interface Reloadable {
    /**
     * 무기의 장탄수를 반환한다.
     *
     * @return 장탄수
     */
    int getCapacity();

    /**
     * 무기의 장전 시간을 반환한다.
     *
     * @return 장전 시간 (tick)
     */
    long getReloadDuration();

    /**
     * 무기 장전 이벤트를 호출한다.
     *
     * @param combatUser       호출한 플레이어
     * @param weaponController 무기 컨트롤러 객체
     */
    void reload(CombatUser combatUser, WeaponController weaponController);
}
