package com.dace.dmgr.system.task;

import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * 태스크 인터페이스.
 *
 * @see TaskWait
 * @see TaskTimer
 */
public interface Task {
    /**
     * @return 스케쥴러 객체
     */
    BukkitTask getBukkitTask();

    /**
     * 태스크 목록을 설정한다.
     *
     * <p>태스크가 끝났을 때 자동으로 목록에서 제거하기 위해 사용한다.</p>
     *
     * @param taskList 태스크 목록
     */
    void setTaskList(List<Task> taskList);
}
