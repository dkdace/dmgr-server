package com.dace.dmgr.combat.action;

/**
 * 상태 변수를 가지고 있는 충전형 스킬의 인터페이스.
 *
 * <p>기본적으로 충전형 스킬은 지속시간이 무한인 토글형이다.</p>
 */
public interface Chargeable extends HasDuration {
    /**
     * 상태 변수의 최댓값을 반환한다.
     *
     * <p>기본값은 {@link Integer#MAX_VALUE}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 상태 변수의 최댓값
     */
    default int getMaxStateValue() {
        return Integer.MAX_VALUE;
    }

    /**
     * 상태 변수의 초당 충전량을 반환한다.
     *
     * @return 상태 변수의 초당 충전량
     */
    int getStateValueIncrement();

    /**
     * 상태 변수의 초당 소모량을 반환한다.
     *
     * @return 상태 변수의 초당 소모량
     */
    int getStateValueDecrement();

    @Override
    default long getDuration() {
        return -1;
    }
}
