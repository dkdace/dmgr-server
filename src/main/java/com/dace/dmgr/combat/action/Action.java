package com.dace.dmgr.combat.action;

import com.dace.dmgr.Disposable;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;

/**
 * 동작(무기, 스킬)의 상태를 관리하는 인터페이스.
 *
 * @see AbstractAction
 */
public interface Action extends Disposable {
    /**
     * 동작의 사용자를 반환한다.
     *
     * @return 플레이어 객체
     */
    @NonNull
    CombatUser getCombatUser();

    /**
     * 동작 태스크 실행 객체를 반환한다.
     *
     * <p>쿨타임, 지속시간 등의 스케쥴러 처리를 위해 사용한다.</p>
     *
     * @return 동작 태스크 실행 객체
     */
    @NonNull
    Disposable getTaskRunner();

    /**
     * 동작 사용 우선순위를 반환한다.
     *
     * <p>같은 사용 키를 가진 동작이 있을 경우 우선순위가 높은 동작이 먼저 사용된다.</p>
     *
     * @return 우선순위
     * @implSpec 0
     */
    default int getPriority() {
        return 0;
    }

    /**
     * 기본 사용 키 목록을 반환한다.
     *
     * @return 기본 사용 키 목록
     */
    @NonNull
    ActionKey @NonNull [] getDefaultActionKeys();

    /**
     * 기본 쿨타임을 반환한다.
     *
     * @return 기본 쿨타임 (tick)
     */
    long getDefaultCooldown();

    /**
     * 쿨타임의 남은 시간을 반환한다.
     *
     * @return 쿨타임 (tick)
     */
    long getCooldown();

    /**
     * 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). -1로 설정 시 무한 지속
     */
    void setCooldown(long cooldown);

    /**
     * 쿨타임을 기본 쿨타임으로 설정한다.
     *
     * @see Action#getDefaultCooldown()
     */
    void setCooldown();

    /**
     * 쿨타임을 증가시킨다.
     *
     * @param cooldown 추가할 쿨타임 (tick)
     */
    void addCooldown(long cooldown);

    /**
     * 쿨타임이 끝났는 지 확인한다.
     *
     * @return 쿨타임 종료 여부
     */
    boolean isCooldownFinished();

    /**
     * 동작을 사용할 수 있는 지 확인한다.
     *
     * @return 사용 가능 여부
     */
    boolean canUse();

    /**
     * 동작 사용 시 실행할 작업.
     *
     * @param actionKey 사용 키
     */
    void onUse(@NonNull ActionKey actionKey);

    /**
     * 사용 중인 동작을 강제로 취소할 수 있는 지 확인한다.
     *
     * @return 강제 취소 가능 여부
     * @implSpec {@code true}
     */
    default boolean isCancellable() {
        return true;
    }

    /**
     * 동작 사용이 취소되었을 때 실행할 작업.
     */
    void onCancelled();

    /**
     * 동작의 상태를 초기화한다.
     */
    void reset();
}
