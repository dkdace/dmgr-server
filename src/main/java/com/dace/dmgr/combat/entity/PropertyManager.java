package com.dace.dmgr.combat.entity;

import lombok.NonNull;

import java.util.EnumMap;

/**
 * 엔티티의 속성 목록을 관리하는 클래스.
 *
 * @see Property
 */
public final class PropertyManager {
    /** 속성 값 목록 (속성 종류 : 값) */
    private final EnumMap<@NonNull Property, Integer> propertyValueMap = new EnumMap<>(Property.class);

    public int getValue(@NonNull Property property) {
        return propertyValueMap.getOrDefault(property, 0);
    }

    public void setValue(@NonNull Property property, int value) {
        propertyValueMap.put(property, Math.min(value, property.getMax()));
    }

    public void addValue(@NonNull Property property, int value) {
        setValue(property, getValue(property) + value);
    }
}
