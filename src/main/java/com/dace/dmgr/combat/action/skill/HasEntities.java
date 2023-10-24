package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.entity.temporal.Summonable;

import java.util.List;

/**
 * 여러 엔티티를 소환할 수 있는 스킬의 인터페이스.
 *
 * @param <T> {@link Summonable}를 상속받는 타입
 * @see HasEntity
 */
public interface HasEntities<T extends Summonable> extends Skill {
    /**
     * 소환된 엔티티 목록을 반환한다.
     *
     * @return 소환된 엔티티 목록
     */
    List<T> getSummonEntities();

    /**
     * 소환된 엔티티를 모두 제거한다.
     */
    default void clearSummonEntities() {
        getSummonEntities().clear();
    }
}