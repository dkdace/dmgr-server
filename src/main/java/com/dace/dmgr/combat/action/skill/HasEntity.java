package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.entity.SummonEntity;

/**
 * 엔티티를 소환할 수 있는 스킬의 인터페이스.
 *
 * @param <T> {@link SummonEntity}를 상속받는 타입
 * @see HasEntities
 */
public interface HasEntity<T extends SummonEntity<?>> extends Skill {
    /**
     * @return 소환된 엔티티
     */
    T getSummonEntity();

    /**
     * @param summonEntity 소환된 엔티티
     */
    void setSummonEntity(T summonEntity);
}