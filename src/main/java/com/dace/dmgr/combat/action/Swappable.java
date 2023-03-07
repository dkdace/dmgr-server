package com.dace.dmgr.combat.action;

/**
 * 2중 탄창 무기의 인터페이스
 */
public interface Swappable {
    /**
     * 보조무기를 반환한다.
     * @return 보조무기
     */
    Weapon getSubweapon();

    /**
     * 보조무기의 쿨타임을 반환한다.
     * @return 쿨타임
     */
    default long getSubweaponCooldown() {
        return getSubweapon().getCooldown();
    };

    /**
     * 무기 교체시간을 반환한다.
     * @return 무기 교체시간 (tick)
     */
    long getSwapDuration();

    enum State {
        PRIMARY,
        SWAPPING,
        SECONDARY
    }
}
