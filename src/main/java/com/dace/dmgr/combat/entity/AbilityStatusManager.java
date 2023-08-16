package com.dace.dmgr.combat.entity;

import java.util.EnumMap;

/**
 * 엔티티의 능력치 정보 목록을 관리하는 클래스.
 */
public final class AbilityStatusManager {
    /** 능력치 정보 목록 (능력치 종류 : 능력치 정보) */
    private final EnumMap<Ability, AbilityStatus> propertyStatusMap = new EnumMap<>(Ability.class);

    /**
     * 인스턴스를 생성한다.
     */
    public AbilityStatusManager() {
        for (Ability ability : Ability.values())
            propertyStatusMap.put(ability, new AbilityStatus(0));
    }

    public AbilityStatus getAbilityStatus(Ability ability) {
        return propertyStatusMap.get(ability);
    }
}
