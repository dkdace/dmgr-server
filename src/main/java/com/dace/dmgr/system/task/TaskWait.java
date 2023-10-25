package com.dace.dmgr.system.task;

import com.dace.dmgr.DMGR;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * 딜레이 기능을 제공하는 클래스.
 *
 * <p>Example:</p>
 *
 * <pre>{@code
 * new TaskWait(5) {
 *     // 지정한 딜레이만큼 기다린 후 호출된다.
 *     @Override
 *     public void run() {
 *         // 실행할 구문
 *     }
 * }
 * }</pre>
 */
public abstract class TaskWait implements Task {
    /** 딜레이 */
    protected final long delay;
    /** 스케쥴러 객체 */
    @Getter
    private final BukkitTask bukkitTask;
    /** 태스크 목록 */
    @Setter
    private List<Task> taskList = null;

    /**
     * 지정한 딜레이만큼 기다린 후 {@link TaskWait#onEnd()}을 호출한다.
     *
     * @param delay 딜레이 (tick)
     */
    protected TaskWait(long delay) {
        this.delay = delay;

        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                preEnd();
            }
        }.runTaskLater(DMGR.getPlugin(), delay);
    }

    /**
     * @see TaskWait#onEnd()
     */
    private void preEnd() {
        if (taskList != null)
            taskList.remove(this);
        onEnd();
    }

    /**
     * 딜레이가 끝났을 때 호출된다.
     */
    protected abstract void onEnd();
}
