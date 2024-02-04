package com.dace.dmgr;

/**
 * 폐기할 수 있는 개체의 인터페이스.
 */
public interface Disposable extends Checkable {
    /**
     * @throws CannotAccessException 인스턴스 접근이 불가능할 때 발생
     * @implSpec {@link Disposable#isDisposed()}가 {@code true}면 {@link CannotAccessException} 발생
     */
    default void checkAccess() {
        if (isDisposed())
            throw new CannotAccessException("인스턴스가 폐기됨");
    }

    /**
     * 인스턴스를 폐기한다.
     */
    void dispose();

    /**
     * 인스턴스가 폐기 되었는지 확인한다.
     *
     * @return 폐기 여부
     */
    boolean isDisposed();
}
