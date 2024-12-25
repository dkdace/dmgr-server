package com.dace.dmgr.util.task;

import com.dace.dmgr.Disposable;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

import java.util.HashSet;

/**
 * 태스크({@link Task})의 일괄 종료 기능을 제공하는 클래스.
 *
 * <p>동시에 실행 중인 여러 태스크를 안전하게 종료하기 위해 사용한다.</p>
 *
 * @see Task
 */
public final class TaskManager implements Disposable {
    /** 태스크 목록 */
    private final HashSet<Task> tasks = new HashSet<>();
    /** 비활성화 여부 */
    @Getter
    private boolean isDisposed = false;

    /**
     * 새로운 태스크를 추가한다.
     *
     * @param task 태스크
     * @throws IllegalStateException 해당 {@code task}가 이미 추가되었으면 발생
     */
    public void add(@NonNull Task task) {
        task.validate();
        Validate.validState(tasks.add(task));

        tasks.removeIf(Task::isDisposed);
    }

    /**
     * 추가된 작동 중인 모든 태스크를 종료한다.
     */
    public void dispose() {
        tasks.stream().filter(task -> !task.isDisposed).forEach(Task::dispose);
        tasks.clear();
        isDisposed = true;
    }
}
