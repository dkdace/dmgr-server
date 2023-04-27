package com.dace.dmgr.combat.action;

/**
 * 주무기와 보조무기의 전환이 가능한 2중 무기의 인터페이스.
 */
public interface Swappable {
    /**
     * 보조무기를 반환한다.
     *
     * @return 보조무기
     */
    Weapon getSubweapon();

    /**
     * 보조무기의 쿨타임을 반환한다.
     *
     * @return 쿨타임 (tick)
     */
    default long getSubweaponCooldown() {
        return getSubweapon().getCooldown();
    }

    /**
     * 무기 교체시간을 반환한다.
     *
     * @return 무기 교체시간 (tick)
     */
    long getSwapDuration();

    /**
     * 현재 무기의 상태.
     */
    enum State {
        /** 주무기 사용 중 */
        PRIMARY,
        /** 교체 중 */
        SWAPPING,
        /** 보조무기 사용 중 */
        SECONDARY
    }
}
