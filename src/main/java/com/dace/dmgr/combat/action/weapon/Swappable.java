package com.dace.dmgr.combat.action.weapon;

/**
 * 주무기와 보조무기의 전환이 가능한 2중 무기의 인터페이스.
 */
public interface Swappable {
    /**
     * 보조무기를 반환한다.
     *
     * @return 보조무기 객체
     */
    <T extends Weapon> T getSubweapon();

    /**
     * @return 무기 전환 상태
     */
    WeaponState getWeaponState();

    /**
     * 무기 교체시간을 반환한다.
     *
     * @return 무기 교체시간 (tick)
     */
    long getSwapDuration();

    /**
     * 무기 교체 이벤트를 호출한다.
     */
    void swap();

    /**
     * 무기 전환 상태 목록.
     */
    enum WeaponState {
        /** 주무기 사용 중 */
        PRIMARY,
        /** 보조무기 사용 중 */
        SECONDARY,
        /** 교체 중 */
        SWAPPING,
    }
}