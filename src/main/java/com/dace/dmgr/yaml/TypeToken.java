package com.dace.dmgr.yaml;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * 섹션 항목의 타입 추론을 위한 타입 토큰 클래스.
 *
 * @param <T> 역직렬화된 데이터 타입
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class TypeToken<T> {
    private final Type type;

    /**
     * 타입 토큰 인스턴스를 생성한다.
     */
    protected TypeToken() {
        Type genericType = getClass().getGenericSuperclass();
        Validate.validState(genericType instanceof ParameterizedType, "제네릭 타입이 유효하지 않음");

        this.type = ((ParameterizedType) genericType).getActualTypeArguments()[0];
    }

    /**
     * 원시 타입 (클래스)을 반환한다.
     *
     * @return 원시 타입
     */
    @SuppressWarnings("unchecked")
    Class<T> getRawType() {
        return (Class<T>) TypeUtils.getRawType(type, null);
    }

    /**
     * 중첩된 제네릭 타입의 타입 토큰 목록을 반환한다.
     *
     * @return 타입 토큰 목록. 존재하지 않으면 {@code null} 반환
     */
    TypeToken<?> @Nullable [] getNestedTypeTokens() {
        if (!(type instanceof ParameterizedType))
            return null;

        return Arrays.stream(((ParameterizedType) type).getActualTypeArguments()).map(t -> new TypeToken<T>(t) {
        }).toArray(TypeToken[]::new);
    }
}
