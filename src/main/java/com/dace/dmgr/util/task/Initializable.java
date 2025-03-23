package com.dace.dmgr.util.task;

import lombok.NonNull;

/**
 * 초기화가 필요한 개체의 인터페이스.
 *
 * <p>초기화 시 비동기 작업이 필요한 클래스를 위해 사용한다.</p>
 *
 * @param <T> {@link Initializable#init()}에서 반환할 값의 타입
 * @see AsyncTask
 */
public interface Initializable<T> {
    /**
     * 인스턴스의 접근 유효성을 확인한다.
     *
     * <p>확인이 필요한 메소드의 첫 번째 줄에 추가해야 한다.</p>
     *
     * @throws IllegalStateException 인스턴스 접근이 불가능할 때 발생
     * @implSpec {@link Initializable#isInitialized()}가 {@code true}면 {@link IllegalStateException} 발생
     */
    default void validate() {
        if (!isInitialized())
            throw new IllegalStateException("인스턴스가 아직 초기화되지 않음");
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
