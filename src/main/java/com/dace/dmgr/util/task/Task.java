package com.dace.dmgr.util.task;

import com.dace.dmgr.Disposable;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

/**
 * 스케쥴러를 실행하는 태스크 인터페이스.
 */
public abstract class Task implements Disposable {
    /** 비활성화 여부 */
    @Getter
    boolean isDisposed = false;
    /** 스케쥴러 객체 */
    private BukkitTask bukkitTask;

    /**
     * 태스크 스케쥴러를 실행한다.
     */
    public final void run() {
        checkAccess();
        bukkitTask = getBukkitTask();
    }

    /**
     * 작동 중인 태스크를 강제로 종료한다.
     */
    @Override
    public final void dispose() {
        if (isDisposed)
            return;

        bukkitTask.cancel();
        isDisposed = true;
    }

    /**
     * Bukkit이 실행하는 태스크 객체를 반환한다.
     *
     * @return 스케쥴러 태스크 객체
     */
    abstract BukkitTask getBukkitTask();
}
