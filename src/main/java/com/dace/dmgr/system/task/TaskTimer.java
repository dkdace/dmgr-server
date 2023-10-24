package com.dace.dmgr.system.task;

import com.dace.dmgr.DMGR;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * 타이머 기능을 제공하는 클래스.
 *
 * <p>Example:</p>
 *
 * <pre>{@code
 * new TaskTimer(1, 5) {
 *     // 지정한 횟수만큼 반복하여 호출된다.
 *     @Override
 *     public boolean run(int i) {
 *         // 실행할 구문
 *
 *         // 실행을 멈추고 다음 주기로 넘어가려면 true를 반환해야 한다.
 *         // for문으로 치면 continue이다.
 *         return true;
 *
 *         // 실행을 멈추고 타이머를 끝내려면 false를 반환해야 한다.
 *         // for문으로 치면 break이다.
 *         return false;
 *     }
 *
 *     // 타이머가 끝났을 때 호출된다.
 *     @Override
 *     public void onEnd(boolean cancelled) {
 *         // 실행할 구문
 *     }
 * }
 * }</pre>
 */
public abstract class TaskTimer implements Task {
    /** 실행 주기 */
    protected final long period;
    /** 반복 횟수 */
    protected final long repeat;
    /** 스케쥴러 객체 */
    @Getter
    private final BukkitTask bukkitTask;
    /** 태스크 목록 */
    @Setter
    private List<Task> taskList = null;

    /**
     * 지정한 횟수만큼 {@link TaskTimer#run(int)}을 반복하여 호출한다.
     *
     * @param period 실행 주기 (tick)
     * @param repeat 반복 횟수. {@code 0}으로 설정 시 무한 반복
     */
    protected TaskTimer(long period, long repeat) {
        this.period = period;
        this.repeat = repeat;

        bukkitTask = new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (!TaskTimer.this.run(i++)) {
                    cancel();
                    onPreEnd(true);
                    return;
                }

                if (repeat > 0 && (i >= repeat)) {
                    cancel();
                    onPreEnd(false);
                }
            }
        }.runTaskTimer(DMGR.getPlugin(), 0, period);
    }

    /**
     * {@link TaskTimer#run(int)}을 무한 반복하여 호출한다.
     *
     * @param period 실행 주기 (tick)
     */
    protected TaskTimer(long period) {
        this(period, 0);
    }

    /**
     * @see TaskTimer#onEnd(boolean)
     */
    public void onPreEnd(boolean cancelled) {
        if (taskList != null)
            taskList.remove(this);
        onEnd(cancelled);
    }

    /**
     * 타이머가 끝났을 때 호출된다.
     *
     * @param cancelled 취소 여부. {@link TaskTimer#run(int)}에서 {@code false}를 반환하여 끝낸 경우 {@code true}이다.
     */
    public void onEnd(boolean cancelled) {
    }

    /**
     * @param i 인덱스 (0부터 시작)
     * @return 다음 주기로 넘어가려면 {@code true} 반환, 타이머를 종료하려면 {@code false} 반환
     */
    public abstract boolean run(int i);
}
