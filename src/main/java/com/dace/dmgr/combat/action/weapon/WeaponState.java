package com.dace.dmgr.combat.action.weapon;

public enum WeaponState {
    /** 주무기 사용 중 */
    PRIMARY,
    /** 보조무기 사용 중 */
    SECONDARY,
    /** 교체 중 */
    SWAPPING,
    /** 재장전 중 */
    RELOADING,
}
