package com.dace.dmgr.util.task;

import com.dace.dmgr.DMGR;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 일정 주기마다 작업을 수행하는 태스크를 실행하는 클래스.
 *
 * <p>Example:</p>
 *
 * <pre>{@code
 * new IntervalTask(i -> {
 *     // 지정한 횟수만큼 반복하여 호출된다.
 *
 *     // 실행을 멈추고 다음 주기로 넘어가려면 true를 반환해야 한다.
 *     // for문으로 치면 continue이다.
 *     return true;
 *
 *     // 실행을 멈추고 타이머를 끝내려면 false를 반환해야 한다.
 *     // for문으로 치면 break이다.
 *     return false;
 * }, isCancelled -> {
 *     // 타이머가 끝났을 때 호출된다.
 * }, 5).run();
 * }</pre>
 */
@AllArgsConstructor
public final class IntervalTask extends Task {
    /**
     * 매 주기마다 실행할 작업.
     *
     * <p>인덱스 (0부터 시작)를 인자로 받으며, 다음 주기로 넘어가려면 {@code true} 반환,
     * 타이머를 종료하려면 {@code false} 반환</p>
     */
    @NonNull
    private final Function<@NonNull Long, @NonNull Boolean> onCycle;
    /**
     * 태스크가 끝났을 때 실행할 작업.
     *
     * <p>{@link IntervalTask#onCycle}에서 {@code false}를 반환하여 끝낸 경우
     * {@code true}를 인자로 받음</p>
     */
    @NonNull
    private final Consumer<@NonNull Boolean> onFinish;
    /** 실행 주기 */
    private final long period;
    /** 반복 횟수 */
    private final long repeat;

    /**
     * 무한 반복하는 태스크 인스턴스를 생성한다.
     *
     * @param onCycle  매 주기마다 실행할 작업.
     *
     *                 <p>인덱스 (0부터 시작)를 인자로 받으며, 다음 주기로 넘어가려면 {@code true} 반환,
     *                 타이머를 종료하려면 {@code false} 반환</p>
     * @param onFinish 태스크가 끝났을 때 실행할 작업.
     *
     *                 <p>{@link IntervalTask#onCycle}에서 {@code false}를 반환하여 끝낸 경우
     *                 {@code true}를 인자로 받음</p>
     * @param period   실행 주기 (tick)
     */
    public IntervalTask(@NonNull Function<@NonNull Long, @NonNull Boolean> onCycle, @NonNull Consumer<@NonNull Boolean> onFinish, long period) {
        this(onCycle, onFinish, period, 0);
    }

    /**
     * 태스크 인스턴스를 생성한다.
     *
     * @param onCycle 매 주기마다 실행할 작업.
     *
     *                <p>인덱스 (0부터 시작)를 인자로 받으며, 다음 주기로 넘어가려면 {@code true} 반환,
     *                타이머를 종료하려면 {@code false} 반환</p>
     * @param period  실행 주기 (tick)
     * @param repeat  반복 횟수
     */
    public IntervalTask(@NonNull Function<@NonNull Long, @NonNull Boolean> onCycle, long period, long repeat) {
        this(onCycle, isCancelled -> {
        }, period, repeat);
    }

    /**
     * 무한 반복하는 태스크 인스턴스를 생성한다.
     *
     * @param onCycle 매 주기마다 실행할 작업.
     *
     *                <p>인덱스 (0부터 시작)를 인자로 받으며, 다음 주기로 넘어가려면 {@code true} 반환,
     *                타이머를 종료하려면 {@code false} 반환</p>
     * @param period  실행 주기 (tick)
     */
    public IntervalTask(@NonNull Function<@NonNull Long, @NonNull Boolean> onCycle, long period) {
        this(onCycle, isCancelled -> {
        }, period, 0);
    }

    @Override
    @NotNull
    BukkitTask getBukkitTask() {
        return new BukkitRunnable() {
            long i = 0;

            @Override
            public void run() {
                if (!onCycle.apply(i++)) {
                    end(true);
                    return;
                }

                if (repeat > 0 && (i >= repeat))
                    end(false);
            }

            private void end(boolean isCancelled) {
                onFinish.accept(isCancelled);
                dispose();
            }
        }.runTaskTimer(DMGR.getPlugin(), 0, period);
    }
}
