package com.dace.dmgr.util.task;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.scheduler.BukkitTask;

/**
 * 스케쥴러를 실행하는 태스크 클래스.
 */
public abstract class Task {
    /** 종료 여부 */
    @Getter
    private boolean isStopped = false;
    /** 스케쥴러 객체 */
    private BukkitTask bukkitTask;

    /**
     * 태스크 스케쥴러를 실행한다.
     */
    final void run() {
        bukkitTask = getBukkitTask();
    }

    /**
     * 작동 중인 태스크를 강제로 종료한다.
     */
    public final void stop() {
        if (isStopped)
            return;

        if (bukkitTask != null)
            bukkitTask.cancel();
        isStopped = true;
    }

    /**
     * Bukkit이 실행하는 태스크 인스턴스를 반환한다.
     *
     * @return 스케쥴러 태스크 인스턴스
     */
    @NonNull
    abstract BukkitTask getBukkitTask();
}
