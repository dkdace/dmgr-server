package com.dace.dmgr.util.task;

import com.dace.dmgr.Disposable;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.WeakHashMap;

/**
 * 태스크({@link Task})를 저장하고 관리하는 클래스.
 *
 * <p>플레이어의 퇴장 등으로 실행 중인 태스크를 즉시 중단하기 위해 사용한다.</p>
 *
 * @see Task
 */
@UtilityClass
public final class TaskUtil {
    /** 태스크 목록 (실행 주체 : 태스크 목록) */
    private static final WeakHashMap<@NonNull Disposable, HashSet<Task>> taskMap = new WeakHashMap<>();

    /**
     * 지정한 객체가 실행하는 태스크를 추가한다.
     *
     * @param object 태스크를 실행하는 객체
     * @param task   태스크 객체
     * @throws IllegalStateException 해당 {@code task}가 이미 추가되었으면 발생
     */
    public static void addTask(@NonNull Disposable object, @NonNull Task task) {
        if (object.isDisposed())
            return;

        taskMap.putIfAbsent(object, new HashSet<>());
        HashSet<Task> tasks = taskMap.get(object);
        if (!tasks.add(task))
            throw new IllegalStateException("해당 Task가 이미 추가됨");
    }

    /**
     * 지정한 객체가 실행하는 모든 태스크를 중지한다.
     *
     * @param object 태스크를 실행하는 객체
     */
    public static void clearTask(@NonNull Disposable object) {
        HashSet<Task> tasks = taskMap.get(object);
        if (tasks == null)
            return;

        tasks.stream().filter(task -> !task.isDisposed).forEach(Task::dispose);
        taskMap.remove(object);
    }
}
