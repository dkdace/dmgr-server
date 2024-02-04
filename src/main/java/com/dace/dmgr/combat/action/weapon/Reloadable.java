package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import lombok.NonNull;

/**
 * 재장전 가능한 무기의 인터페이스.
 */
public interface Reloadable extends Weapon {
    /**
     * @return 재장전 모듈
     */
    @NonNull
    ReloadModule getReloadModule();

    /**
     * 무기를 재장전할 수 있는 지 확인한다.
     *
     * <p>기본값은 {@code true}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 재장전 가능 여부
     */
    default boolean canReload() {
        return true;
    }

    /**
     * 모든 탄약을 소모했을 때 실행할 작업.
     */
    void onAmmoEmpty();

    /**
     * 재장전을 진행할 때 매 tick마다 실행할 작업.
     *
     * @param i 인덱스
     */
    void onReloadTick(long i);

    /**
     * 재장전이 끝났을 때 실행할 작업.
     */
    void onReloadFinished();
}
