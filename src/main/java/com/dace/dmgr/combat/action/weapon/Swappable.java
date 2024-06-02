package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.weapon.module.SwapModule;
import lombok.NonNull;

/**
 * 주무기와 보조무기의 전환이 가능한 2중 무기의 인터페이스.
 *
 * @param <T> {@link Weapon}을 상속받는 보조무기
 */
public interface Swappable<T extends Weapon> extends Weapon {
    /**
     * @return 2중 무기 모듈
     */
    @NonNull
    SwapModule<T> getSwapModule();

    /**
     * 무기 전환을 시작할 때 실행할 작업.
     *
     * @param swapState 변경할 상태
     */
    void onSwapStart(@NonNull SwapState swapState);

    /**
     * 무기 전환이 끝났을 때 실행할 작업.
     *
     * @param swapState 변경할 상태
     */
    void onSwapFinished(@NonNull SwapState swapState);

    /**
     * 무기 전환 상태 목록.
     */
    enum SwapState {
        /** 주무기 사용 중 */
        PRIMARY,
        /** 보조무기 사용 중 */
        SECONDARY
    }
}