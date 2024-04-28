package com.dace.dmgr.combat.entity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.EnumMap;

/**
 * 엔티티의 속성 목록을 관리하는 클래스.
 *
 * @see Property
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class PropertyManager {
    /** 속성 값 목록 (속성 종류 : 값) */
    private final EnumMap<@NonNull Property, Integer> propertyValueMap = new EnumMap<>(Property.class);

    /**
     * 속성 값을 반환한다.
     *
     * @param property 속성 종류
     * @return 속성 값
     */
    public int getValue(@NonNull Property property) {
        return propertyValueMap.getOrDefault(property, 0);
    }

    /**
     * 속성 값을 설정한다.
     *
     * @param property 속성 종류
     * @param value    속성 값
     */
    public void setValue(@NonNull Property property, int value) {
        propertyValueMap.put(property, Math.min(value, property.getMax()));
    }

    /**
     * 속성 값을 증가시킨다.
     *
     * @param property 속성 종류
     * @param value    속성 값 증가량
     */
    public void addValue(@NonNull Property property, int value) {
        setValue(property, getValue(property) + value);
    }
}
