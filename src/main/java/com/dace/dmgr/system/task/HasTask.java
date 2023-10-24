package com.dace.dmgr.system.task;

/**
 * {@link TaskManager}에 등록하고 태스크를 실행할 수 있는 개체.
 *
 * @see Task
 */
public interface HasTask {
    /**
     * @return 태스크 식별자
     */
    String getTaskIdentifier();
}
