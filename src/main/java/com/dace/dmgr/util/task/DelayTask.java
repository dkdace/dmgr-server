package com.dace.dmgr.util.task;

import com.dace.dmgr.DMGR;
import lombok.NonNull;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * 일정 시간을 기다리는 작업을 수행하는 태스크를 실행하는 클래스.
 *
 * <p>Example:</p>
 *
 * <pre>{@code
 * new DelayTask(() -> {
 *     // 지정한 딜레이만큼 기다린 후 호출된다.
 * }, 5);
 * }</pre>
 */
public final class DelayTask extends Task {
    /**
     * 태스크가 끝났을 때 실행할 작업.
     */
    @NonNull
    private final Runnable onFinish;
    /** 딜레이 (tick) */
    private final long delay;

    /**
     * 비동기 작업을 수행하는 태스크 인스턴스를 생성한다.
     *
     * @param onFinish 태스크가 끝났을 때 실행할 작업.
     * @param delay    딜레이 (tick)
     */
    public DelayTask(@NonNull Runnable onFinish, long delay) {
        this.onFinish = onFinish;
        this.delay = delay;

        run();
    }

    @Override
    @NonNull
    BukkitTask getBukkitTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                onFinish.run();
                dispose();
            }
        }.runTaskLater(DMGR.getPlugin(), delay);
    }
}
