package com.dace.dmgr;

/**
 * 폐기할 수 있는 개체의 인터페이스.
 */
public interface Disposable {
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
