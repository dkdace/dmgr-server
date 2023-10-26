package com.dace.dmgr.system.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 태스크({@link Task})를 저장하고 관리하는 클래스.
 *
 * <p>플레이어의 퇴장 등으로 실행 중인 태스크를 즉시 중단하기 위해 사용한다.</p>
 */
public final class TaskManager {
    private static final Map<String, List<Task>> taskMap = new HashMap<>();

    /**
     * 지정한 {@link HasTask}가 실행하는 태스크를 추가한다.
     *
     * @param hasTask 태스크를 실행할 수 있는 객체
     * @param task    태스크 객체
     */
    public static void addTask(HasTask hasTask, Task task) {
        taskMap.putIfAbsent(hasTask.getTaskIdentifier(), new ArrayList<>());
        List<Task> taskList = taskMap.get(hasTask.getTaskIdentifier());
        taskList.add(task);
        task.setTaskList(taskList);
    }

    /**
     * 지정한 {@link HasTask}가 실행하는 모든 태스크를 중지한다.
     *
     * @param hasTask 태스크를 실행할 수 있는 객체
     */
    public static void clearTask(HasTask hasTask) {
        List<Task> taskList = taskMap.get(hasTask.getTaskIdentifier());
        if (taskList == null)
            return;

        taskList.forEach(task -> task.getBukkitTask().cancel());
        taskMap.remove(hasTask.getTaskIdentifier());
    }
}
