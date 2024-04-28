package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 무기의 연사 모듈 클래스.
 *
 * <p>무기가 {@link FullAuto}를 상속받는 클래스여야 한다.</p>
 *
 * @see FullAuto
 */
@RequiredArgsConstructor
public class FullAutoModule {
    /** 무기 객체 */
    @NonNull
    protected final FullAuto weapon;
    /** 연사 기능을 적용할 동작 사용 키 */
    @NonNull
    @Getter
    protected final ActionKey fullAutoKey;
    /** 연사속도 */
    @NonNull
    protected final FullAuto.FireRate fireRate;

    /**
     * 틱을 기준으로 발사할 수 있는 시점을 확인한다.
     *
     * @param tick 기준 틱
     * @return 발사 가능 여부
     */
    public final boolean isFireTick(long tick) {
        tick = tick % 20;

        return (fireRate.getTickFlag() & (1 << tick)) != 0;
    }
}
