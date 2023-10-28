package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.weapon.module.ReloadModule;

/**
 * 재장전 가능한 무기의 인터페이스.
 */
public interface Reloadable extends Weapon {
    /**
     * @return 재장전 모듈
     */
    ReloadModule getReloadModule();

    /**
     * 무기를 재장전한다.
     *
     * @implSpec {@link ReloadModule#reload()}
     */
    void reload();

    /**
     * 재장전을 진행할 때 (매 tick마다) 실행할 작업.
     *
     * @param i 인덱스
     */
    default void onReloadTick(int i) {
    }

    /**
     * 재장전이 끝났을 때 실행할 작업.
     */
    default void onReloadFinished() {
    }
}
