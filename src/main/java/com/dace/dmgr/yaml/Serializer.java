package com.dace.dmgr.yaml;

import lombok.NonNull;

/**
 * 섹션 항목의 직렬화 및 역직렬화를 관리하는 인터페이스.
 *
 * @param <T> 역직렬화된 데이터 타입
 * @param <R> Yaml 파일에 저장할 직렬화된 데이터 타입
 */
public interface Serializer<T, R> {
    /**
     * 지정한 값을 직렬화한다.
     *
     * @param value 값
     * @return 직렬화된 값
     */
    @NonNull
    R serialize(@NonNull T value);

    /**
     * 지정한 값을 역직렬화한다.
     *
     * @param value 값
     * @return 역직렬화된 값
     */
    @NonNull
    T deserialize(@NonNull R value);
}
