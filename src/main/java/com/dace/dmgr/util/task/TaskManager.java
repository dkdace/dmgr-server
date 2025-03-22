package com.dace.dmgr.util.task;

import lombok.NoArgsConstructor;
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
@NoArgsConstructor
public final class TaskManager {
    /** 태스크 목록 */
    private final HashSet<Task> tasks = new HashSet<>();

    /**
     * 새로운 태스크를 추가한다.
     *
     * @param task 태스크
     * @throws IllegalStateException 해당 {@code task}가 이미 추가되었으면 발생
     */
    public void add(@NonNull Task task) {
        if (task.isStopped())
            return;

        Validate.validState(tasks.add(task), "task가 이미 추가됨");

        tasks.removeIf(Task::isStopped);
    }

    /**
     * 추가된 작동 중인 모든 태스크를 종료한다.
     */
    public void stop() {
        tasks.forEach(Task::stop);
        tasks.clear();
    }
}
