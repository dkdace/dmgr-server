package com.dace.dmgr;

import java.text.MessageFormat;

/**
 * 메소드 호출 시 확인이 필요한 개체의 인터페이스.
 */
public interface Checkable {
    /**
     * 인스턴스에 접근을 시도한다.
     *
     * <p>확인이 필요한 메소드의 첫 번째 줄에 추가해야 한다.</p>
     */
    void checkAccess();

    /**
     * 인스턴스 접근이 불가능할 때 발생하는 예외.
     */
    class CannotAccessException extends IllegalStateException {
        public CannotAccessException(String message) {
            super(MessageFormat.format("접근 불가 : {0}", message));
        }

        public CannotAccessException() {
            this("잘못된 접근");
        }
    }
}
