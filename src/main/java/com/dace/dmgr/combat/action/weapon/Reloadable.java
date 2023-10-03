package com.dace.dmgr.combat.action.weapon;

/**
 * 재장전 가능한 무기의 인터페이스.
 */
public interface Reloadable {
    /**
     * @return 남은 탄약 수
     */
    int getRemainingAmmo();

    /**
     * @param remainingAmmo 남은 탄약 수
     */
    void setRemainingAmmo(int remainingAmmo);

    /**
     * @return 재장전 상태
     */
    boolean isReloading();

    /**
     * 무기의 재장전을 취소한다.
     */
    void cancelReloading();

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
     */
    void reload();
}
