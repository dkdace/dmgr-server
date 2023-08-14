package com.dace.dmgr.system.task;

import com.dace.dmgr.DMGR;
import org.bukkit.scheduler.BukkitRunnable;

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
public abstract class TaskWait {
    /** 딜레이 */
    protected final long delay;

    /**
     * 지정한 딜레이만큼 기다린 후 {@link TaskWait#run()}을 호출한다.
     *
     * @param delay 딜레이 (tick)
     */
    protected TaskWait(long delay) {
        this.delay = delay;
        execute();
    }

    private void execute() {
        new BukkitRunnable() {
            @Override
            public void run() {
                TaskWait.this.run();

            }
        }.runTaskLater(DMGR.getPlugin(), delay);
    }

    public abstract void run();
}
