package com.dace.dmgr.combat.ability.weapon;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.weapon.module.SwapModule;
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
    SwapModule<@NonNull T> getSwapModule();

    /**
     * 보조무기를 반환한다.
     *
     * @return 보조무기 인스턴스
     */
    @NonNull
    T getSubweapon();

    /**
     * 무기 교체 시간을 반환한다.
     *
     * @return 무기 교체 시간
     */
    @NonNull
    Timespan getSwapDuration();

    /**
     * 무기 전환을 시작할 때 실행할 작업.
     *
     * @param isSwapped 보조무기 전환 상태
     */
    void onSwapStart(boolean isSwapped);

    /**
     * 무기 전환이 끝났을 때 실행할 작업.
     *
     * @param isSwapped 보조무기 전환 상태
     */
    void onSwapFinished(boolean isSwapped);
}