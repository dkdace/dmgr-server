package com.dace.dmgr.util.task;

import com.dace.dmgr.Checkable;
import lombok.NonNull;

/**
 * 초기화가 필요한 개체의 인터페이스.
 *
 * <p>초기화 시 비동기 작업이 필요한 클래스를 위해 사용한다.</p>
 *
 * @see AsyncTask
 */
public interface Initializable<T> extends Checkable {
    /**
     * @throws CannotAccessException 인스턴스 접근이 불가능할 때 발생
     * @implSpec {@link Initializable#isInitialized()}가 {@code false}면 {@link CannotAccessException} 발생
     */
    @Override
    default void checkAccess() {
        if (!isInitialized())
            throw new CannotAccessException("인스턴스가 아직 초기화되지 않음");
    }

    /**
     * 인스턴스를 초기화한다.
     *
     * @return 비동기 태스크
     */
    @NonNull
    AsyncTask<T> init();

    /**
     * 인스턴스가 초기화 되었는지 확인한다.
     *
     * @return 초기화 여부
     */
    boolean isInitialized();
}
