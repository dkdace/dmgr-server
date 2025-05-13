package com.dace.dmgr.util.task;

import com.dace.dmgr.DMGR;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
 * <pre><code>
 * new AsyncTask&lt;Integer&gt;(() -&gt; {
 *     // 별도의 스레드에서 비동기 작업을 수행한다.
 *
 *     try {
 *         // 작업 성공 시 값을 반환해야 한다.
 *         return 1;
 *     } catch (Exception ex) {
 *         // 작업 실패 시 예외를 던져야 한다.
 *         throw new IllegalStateException(ex);
 *     }
 * }).onFinish(result -&gt; {
 *     // 작업 성공 시 호출된다.
 * }).onError(ex -&gt; {
 *     // 작업 실패(예외 발생) 시 호출된다.
 * });
 * </code></pre>
 *
 * @param <T> 태스크 종료 시 반환되는 값의 타입
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AsyncTask<T> extends Task {
    /** 메서드 체이닝을 위해 사용하는 Future 인스턴스 */
    private final CompletableFuture<T> future;
    /**
     * 태스크에서 성공 및 실패 시 실행할 작업
     *
     * <p>성공을 나타내려면 결과 값을 반환, 실패를 나타내려면 예외 던지기</p>
     */
    private Supplier<T> onInit;

    /**
     * 비동기 작업을 수행하는 태스크 인스턴스를 생성한다.
     *
     * @param onInit 태스크에서 성공 및 실패 시 실행할 작업.
     *
     *               <p>성공을 나타내려면 결과 값을 반환, 실패를 나타내려면 예외 던지기</p>
     */
    public AsyncTask(@NonNull Supplier<T> onInit) {
        this.future = new CompletableFuture<>();
        this.onInit = onInit;

        run();
    }

    /**
     * 작업 성공 시 실행할 비동기 작업을 지정한다.
     *
     * @param action 태스크가 끝났을 때 실행할 작업.
     *
     *               <p>이전 단계에서 반환한 값을 인자로 받으며, 비동기 작업을 처리할 {@link AsyncTask}을 반환해야 함</p>
     * @param <U>    다음 {@code onFinish} 호출에 사용되는 반환 타입
     * @return 새로운 {@link AsyncTask}
     * @see AsyncTask#onFinish(Function)
     */
    @NonNull
    public <U> AsyncTask<U> onFinishAsync(@NonNull Function<T, @NonNull AsyncTask<U>> action) {
        return new AsyncTask<>(future.thenCompose(result -> action.apply(result).future));
    }

    /**
     * 작업 성공 시 실행할 비동기 작업을 지정한다.
     *
     * @param action 태스크가 끝났을 때 실행할 작업.
     *
     *               <p>비동기 작업을 처리할 {@link AsyncTask}을 반환해야 함</p>
     * @param <U>    다음 {@code onFinish} 호출에 사용되는 반환 타입
     * @return 새로운 {@link AsyncTask}
     * @see AsyncTask#onFinish(Supplier)
     */
    @NonNull
    public <U> AsyncTask<U> onFinishAsync(@NonNull Supplier<@NonNull AsyncTask<U>> action) {
        return onFinishAsync(t -> action.get());
    }

    /**
     * 작업 성공 시 실행할 작업을 지정한다.
     *
     * @param action 태스크가 끝났을 때 실행할 작업.
     *
     *               <p>이전 단계에서 반환한 값을 인자로 받으며, 다음 onFinish에 사용할 값을 반환해야 함</p>
     * @param <U>    다음 {@code onFinish} 호출에 사용되는 반환 타입
     * @return 새로운 {@link AsyncTask}
     * @see AsyncTask#onFinish(Consumer)
     * @see AsyncTask#onFinish(Supplier)
     * @see AsyncTask#onFinish(Runnable)
     */
    @NonNull
    public <U> AsyncTask<U> onFinish(@NonNull Function<T, U> action) {
        return new AsyncTask<>(future.thenApplyAsync(action, SyncExecutor.instance));
    }

    /**
     * 작업 성공 시 실행할 작업을 지정한다.
     *
     * @param action 태스크가 끝났을 때 실행할 작업.
     *
     *               <p>이전 단계에서 반환한 값을 인자로 받음</p>
     * @return 새로운 {@link AsyncTask}
     * @see AsyncTask#onFinish(Function)
     * @see AsyncTask#onFinish(Supplier)
     * @see AsyncTask#onFinish(Runnable)
     */
    @NonNull
    public AsyncTask<Void> onFinish(@NonNull Consumer<T> action) {
        return new AsyncTask<>(future.thenAcceptAsync(action, SyncExecutor.instance));
    }

    /**
     * 작업 성공 시 실행할 작업을 지정한다.
     *
     * @param action 태스크가 끝났을 때 실행할 작업.
     *
     *               <p>다음 onFinish에 사용할 값을 반환해야 함</p>
     * @param <U>    다음 {@code onFinish} 호출에 사용되는 반환 타입
     * @return 새로운 {@link AsyncTask}
     * @see AsyncTask#onFinish(Function)
     * @see AsyncTask#onFinish(Consumer)
     * @see AsyncTask#onFinish(Runnable)
     */
    @NonNull
    public <U> AsyncTask<U> onFinish(@NonNull Supplier<U> action) {
        return onFinish((Function<T, U>) t -> action.get());
    }

    /**
     * 작업 성공 시 실행할 작업을 지정한다.
     *
     * @param action 태스크가 끝났을 때 실행할 작업.
     * @return 새로운 {@link AsyncTask}
     * @see AsyncTask#onFinish(Function)
     * @see AsyncTask#onFinish(Consumer)
     * @see AsyncTask#onFinish(Supplier)
     */
    @NonNull
    public AsyncTask<Void> onFinish(@NonNull Runnable action) {
        return new AsyncTask<>(future.thenRunAsync(action, SyncExecutor.instance));
    }

    /**
     * 작업 실패(예외 발생) 시 실행할 작업을 지정한다.
     *
     * @param action 예외 발생 시 실행할 작업. 예외를 인자로 받음
     * @return 새로운 {@link AsyncTask}
     */
    @NonNull
    public AsyncTask<Void> onError(@NonNull Consumer<@NonNull Exception> action) {
        CompletableFuture<Void> nextFuture = future.exceptionally(ex -> {
            action.accept((Exception) ex);
            return null;
        }).thenAccept(t -> {
        });

        return new AsyncTask<>(nextFuture);
    }

    @Override
    @NonNull
    BukkitTask getBukkitTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    future.complete(onInit.get());
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }

                stop();
            }
        }.runTaskAsynchronously(DMGR.getPlugin());
    }

    /**
     * 동기 작업 실행에 사용되는 클래스.
     */
    private static final class SyncExecutor implements Executor {
        private static final SyncExecutor instance = new SyncExecutor();

        @Override
        public void execute(@NotNull Runnable command) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    command.run();
                }
            }.runTask(DMGR.getPlugin());
        }
    }
}
