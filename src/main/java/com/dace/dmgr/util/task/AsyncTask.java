package com.dace.dmgr.util.task;

import com.dace.dmgr.DMGR;
import lombok.NonNull;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 비동기 작업을 수행하는 태스크를 실행하는 클래스.
 *
 * <p>일반적으로 파일 I/O 등에 사용하며, 비동기 태스크에서 Bukkit API에 접근할 수는 없다.</p>
 *
 * <p>Example:</p>
 *
 * <pre>{@code
 * new AsyncTask<Integer>((onFinish, onError) -> {
 *     // 즉시 호출되며 별도의 스레드에서 비동기 작업을 수행한다.
 *
 *     try {
 *         // 작업 성공 시 호출해야 한다.
 *         onFinish.accept(1);
 *     } catch (Exception ex) {
 *         // 작업 실패(예외 발생) 시 호출해야 한다.
 *         onError.accept(ex);
 *     }
 * }).onFinish(result -> {
 *     // 작업 성공 시 호출된다.
 * }).onError(ex -> {
 *     // 작업 실패(예외 발생) 시 호출된다.
 * });
 * }</pre>
 *
 * @param <T> 태스크 종료 시 반환되는 값의 타입
 */
public final class AsyncTask<T> extends Task {
    /** 메서드 체이닝을 위해 사용하는 Future 객체 */
    private final CompletableFuture<T> future;
    /** 실행할 비동기 작업 */
    private Runnable action;

    /**
     * 비동기 작업을 수행하는 태스크 인스턴스를 생성한다.
     *
     * @param onInit 태스크에서 성공 및 실패 시 실행할 작업.
     *
     *               <p>{@code onFinish} 또는 {@code onError}를 호출하여 결과를 반환해야 함</p>
     */
    public AsyncTask(@NonNull Callback<T> onInit) {
        future = new CompletableFuture<>();
        action = () -> onInit.run(value -> runSync(() -> future.complete(value)), ex -> runSync(() -> future.completeExceptionally(ex)));

        run();
    }

    private AsyncTask(@NonNull CompletableFuture<T> future) {
        this.future = future;
    }

    /**
     * 비동기 작업을 수행하는 태스크 인스턴스를 생성한다.
     *
     * @param asyncTasks 비동기 태스크 목록
     */
    @NonNull
    public static AsyncTask<Void> all(@NonNull AsyncTask<?> @NonNull ... asyncTasks) {
        return new AsyncTask<>(CompletableFuture.allOf(
                Arrays.stream(asyncTasks).map(asyncTask -> asyncTask.future).toArray(CompletableFuture[]::new)));
    }

    /**
     * 비동기 작업을 수행하는 태스크 인스턴스를 생성한다.
     *
     * @param asyncTasks 비동기 태스크 목록
     */
    @NonNull
    public static AsyncTask<Void> all(@NonNull List<@NonNull AsyncTask<?>> asyncTasks) {
        return new AsyncTask<>(CompletableFuture.allOf(
                asyncTasks.stream().map(asyncTask -> asyncTask.future).toArray(CompletableFuture[]::new)));
    }

    /**
     * 작업 성공 시 실행할 비동기 작업을 지정한다.
     *
     * @param action 태스크가 끝났을 때 실행할 비동기 작업.
     *
     *               <p>{@link AsyncTask#AsyncTask(Callback)}에서 호출한
     *               onFinish의 인자값을 인자로 받으며, 다음 onFinish에 사용할 값을 반환해야 함</p>
     * @param <U>    다음 {@code onFinish) 호출에 사용되는 반환 타입
     * @return 새로운 AsyncTask
     */
    @NonNull
    public <U> AsyncTask<U> onFinish(@NonNull Function<T, @NonNull AsyncTask<U>> action) {
        CompletableFuture<U> nextFuture = future.thenCompose(result -> {
            AsyncTask<U> nextAsyncTask = action.apply(result);
            return nextAsyncTask.future;
        });

        return new AsyncTask<>(nextFuture);
    }

    /**
     * 작업 성공 시 실행할 작업을 지정한다.
     *
     * @param action 태스크가 끝났을 때 실행할 작업.
     *
     *               <p>{@link AsyncTask#AsyncTask(Callback)}에서 호출한
     *               onFinish의 인자값을 인자로 받음</p>
     * @return 새로운 AsyncTask
     */
    @NonNull
    public AsyncTask<Void> onFinish(@NonNull Consumer<T> action) {
        CompletableFuture<Void> nextFuture = future.thenAccept(action);
        return new AsyncTask<>(nextFuture);
    }

    /**
     * 작업 성공 시 실행할 작업을 지정한다.
     *
     * @param action 태스크가 끝났을 때 실행할 작업.
     *
     *               <p>다음 onFinish에 사용할 값을 반환해야 함</p>
     * @param <U>    다음 {@code onFinish} 호출에 사용되는 반환 타입
     * @return 새로운 AsyncTask
     */
    @NonNull
    public <U> AsyncTask<U> onFinish(@NonNull Supplier<@NonNull AsyncTask<U>> action) {
        return onFinish(t -> {
            return action.get();
        });
    }

    /**
     * 작업 성공 시 실행할 작업을 지정한다.
     *
     * @param action 태스크가 끝났을 때 실행할 작업.
     * @return 새로운 AsyncTask
     */
    @NonNull
    public AsyncTask<Void> onFinish(@NonNull Runnable action) {
        return onFinish(t -> {
            action.run();
        });
    }

    /**
     * 작업 실패(예외 발생) 시 실행할 작업을 지정한다.
     *
     * @param action 예외 발생 시 실행할 작업. 예외를 인자로 받음
     * @return AsyncTask
     */
    @NonNull
    public AsyncTask<T> onError(@NonNull Consumer<Exception> action) {
        CompletableFuture<T> nextFuture = future.exceptionally(ex -> {
            action.accept((Exception) ex);
            return null;
        });

        return new AsyncTask<>(nextFuture);
    }

    @Override
    @NonNull
    BukkitTask getBukkitTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                action.run();
                dispose();
            }
        }.runTaskAsynchronously(DMGR.getPlugin());
    }

    private void runSync(@NonNull Runnable action) {
        new BukkitRunnable() {
            @Override
            public void run() {
                action.run();
            }
        }.runTask(DMGR.getPlugin());
    }

    /**
     * 성공 및 실패 시 실행할 콜백.
     *
     * @param <T> 반환 타입
     */
    public interface Callback<T> {
        /**
         * 비동기 태스크를 끝낸다.
         *
         * @param onFinish 작업 성공 시 호출해야 한다.
         * @param onError  작업 실패(예외 발생) 시 호출해야 한다.
         */
        void run(@NonNull Consumer<T> onFinish, @NonNull Consumer<@NonNull Exception> onError);
    }
}
