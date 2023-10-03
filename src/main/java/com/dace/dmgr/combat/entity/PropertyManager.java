package com.dace.dmgr.combat.entity;

import java.util.EnumMap;

/**
 * 엔티티의 속성 목록을 관리하는 클래스.
 */
public final class PropertyManager {
    /** 속성 값 목록 (속성 종류 : 값) */
    private final EnumMap<Property, Integer> propertyValueMap = new EnumMap<>(Property.class);

    public int getValue(Property property) {
        return propertyValueMap.getOrDefault(property, 0);
    }

    public void setValue(Property property, int value) {
        propertyValueMap.put(property, Math.min(value, property.getMax()));
    }

    public void addValue(Property property, int value) {
        setValue(property, getValue(property) + value);
    }
}
