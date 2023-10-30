package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.skill.module.HasEntitiesModule;
import com.dace.dmgr.combat.entity.SummonEntity;

/**
 * 여러 엔티티를 소환할 수 있는 스킬의 인터페이스.
 *
 * @param <T> {@link SummonEntity}를 상속받는 타입
 * @see HasEntity
 */
public interface HasEntities<T extends SummonEntity<?>> extends Skill {
    /**
     * @return 엔티티 소환 모듈
     */
    HasEntitiesModule<T> getHasEntitiesModule();
}