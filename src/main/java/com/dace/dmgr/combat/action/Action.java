package com.dace.dmgr.combat.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.task.Task;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * 동작(무기, 스킬)의 상태를 관리하는 인터페이스.
 *
 * @see AbstractAction
 */
public interface Action {
    /**
     * 동작의 사용자를 반환한다.
     *
     * @return 사용자 플레이어
     */
    @NonNull
    CombatUser getCombatUser();

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
     * 액션바에 동작 상태를 표시하기 위한 문자열을 반환한다.
     *
     * @return 동작 상태 표시 문자열. {@code null} 반환 시 표시 대상에서 제외
     * @implSpec {@code null}
     */
    @Nullable
    default String getActionBarString() {
        return null;
    }

    /**
     * 동작에서 실행하는 새로운 태스크를 추가한다.
     *
     * <p>동작이 끊겼을 때 ({@link Action#cancel()} 호출 시) 모든 태스크가 중단된다.</p>
     *
     * @param task 태스크
     * @throws IllegalStateException 해당 {@code task}가 이미 추가되었으면 발생
     * @see Action#addTask(Task)
     */
    void addActionTask(@NonNull Task task);

    /**
     * 동작에서 실행하는 새로운 태스크를 추가한다.
     *
     * <p>동작 강제 취소({@link Action#cancel()} 호출)와 관계 없이 계속 실행한다.</p>
     *
     * @param task 태스크
     * @throws IllegalStateException 해당 {@code task}가 이미 추가되었으면 발생
     * @see Action#addActionTask(Task)
     */
    void addTask(@NonNull Task task);

    /**
     * 동작의 상태가 초기화되었을 때 ({@link Action#reset()} 호출 시) 실행할 작업을 추가한다.
     *
     * @param onReset 실행할 작업
     */
    void addOnReset(@NonNull Runnable onReset);

    /**
     * 동작이 제거되었을 때 ({@link Action#remove()} 호출 시) 실행할 작업을 추가한다.
     *
     * @param onRemove 실행할 작업
     */
    void addOnRemove(@NonNull Runnable onRemove);

    /**
     * 기본 쿨타임을 반환한다.
     *
     * @return 기본 쿨타임
     */
    @NonNull
    Timespan getDefaultCooldown();

    /**
     * 쿨타임의 남은 시간을 반환한다.
     *
     * @return 남은 쿨타임
     */
    @NonNull
    Timespan getCooldown();

    /**
     * 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임
     */
    void setCooldown(@NonNull Timespan cooldown);

    /**
     * 쿨타임을 기본 쿨타임으로 설정한다.
     *
     * @see Action#getDefaultCooldown()
     */
    void setCooldown();

    /**
     * 쿨타임을 증가시킨다.
     *
     * @param cooldown 추가할 쿨타임
     */
    void addCooldown(@NonNull Timespan cooldown);

    /**
     * 쿨타임이 끝났는지 확인한다.
     *
     * @return 쿨타임 종료 여부
     */
    boolean isCooldownFinished();

    /**
     * 동작을 사용할 수 있는지 확인한다.
     *
     * @param actionKey 사용 키
     * @return 사용 가능 여부
     */
    boolean canUse(@NonNull ActionKey actionKey);

    /**
     * 동작 사용 시 실행할 작업.
     *
     * @param actionKey 사용 키
     */
    void onUse(@NonNull ActionKey actionKey);

    /**
     * 사용 중인 동작을 강제로 취소할 수 있는지 확인한다.
     *
     * @return 강제 취소 가능 여부
     * @implSpec {@code true}
     * @see ActionManager#cancelAction(CombatUser)
     */
    default boolean isCancellable() {
        return true;
    }

    /**
     * 동작 사용을 강제로 취소시킨다.
     *
     * <p>{@link Action#isCancellable()}이 {@code true}면 강제 취소가 가능하다.</p>
     *
     * @return 취소 여부. {@link Action#isCancellable()}의 값 반환
     * @see Action#isCancellable()
     */
    boolean cancel();

    /**
     * 동작의 상태를 초기화한다.
     */
    void reset();

    /**
     * 동작을 제거한다.
     *
     * @throws IllegalStateException 이미 제거되었으면 발생
     */
    void remove();
}
