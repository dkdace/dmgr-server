package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;

/**
 * 정조준이 가능한 무기의 인터페이스.
 */
public interface Aimable {
    /**
     * 무기의 조준경 타입을 반환한다.
     *
     * @return 조준경 타입
     */
    int getScope();

    /**
     * 정조준 이벤트를 호출한다.
     *
     * @param combatUser       호출한 플레이어
     * @param weaponController 무기 컨트롤러 객체
     */
    void aim(CombatUser combatUser, WeaponController weaponController);
}